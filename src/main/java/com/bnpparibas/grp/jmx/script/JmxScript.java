package com.bnpparibas.grp.jmx.script;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

/**
 * @author morinb.
 */
public class JmxScript {
   public static final String USAGE = "usage: ";
   public static final int LOGIN_INDEX = 0;
   public static final int PASSWORD_INDEX = 1;
   public static final int SERVER_INDEX = 2;
   public static final int PORT_INDEX = 3;

   private static final String STRING_TYPE = String.class.getName();
   private static final String[] WATCH_METHOD_PARAMS_TYPES = new String[]{STRING_TYPE, STRING_TYPE, STRING_TYPE};

   private static final int EXIT_OK = 0;
   private static final int EXIT_1_UNPARSEABLE_COMMAND_LINE = -1;
   private static final int EXIT_2_NO_RUNTIME = -2;
   private static final int EXIT_3_CREDENTIAL_FILE_PROBLEM = -3;
   private static final int EXIT_4_MISSING_PASSWORD = -4;
   private static final int EXIT_5_MISSING_SERVER = -5;
   private static final int EXIT_6_MISSING_ATTRIBUTE = -5;
   private static final int EXIT_7_UNPARSEABLE_PORT_IN_CREDENTIALS = -7;
   private static final int EXIT_8_UNPARSEABLE_PORT = -8;
   private static final int EXIT_9_MALFORMED_JMX_URL = -9;
   private static final int EXIT_10_MISSING_LOGIN_IN_CREDENTIALS = -10;
   private static final int EXIT_11_MISSING_PASSWORD_IN_CREDENTIALS = -11;
   private static final int EXIT_12_MISSING_SERVER_IN_CREDENTIALS = -12;
   private static final int EXIT_13_MISSING_PORT_IN_CREDENTIALS = -13;

   private static final int EXIT_15_JMX_CONNECTION_FAILED = -15;
   private static final int EXIT_16_MALFORMED_OBJECT = -16;
   private static final int EXIT_17_ATTRIBUTE_NOT_FOUND = -17;
   private static final int EXIT_18_MBEAN_GETTER_EXCEPTION = -18;
   private static final int EXIT_19_MBEAN_SETTER_EXCEPTION = -19;
   private static final int EXIT_20_INSTANCE_NOT_FOUND = -20;
   private static final int EXIT_21_UNABLE_TON_FIND_COMMAND = -21;


   public static final String OPT_LOGIN_SHORT = "l";
   public static final String OPT_LOGIN = "login";

   public static final String OPT_PASSWORD_SHORT = "p";
   public static final String OPT_PASSWORD = "password";

   public static final String OPT_SERVER_SHORT = "s";
   public static final String OPT_SERVER = "server";

   public static final String OPT_PORT_SHORT = "o";
   public static final String OPT_PORT = "port";

   public static final String OPT_RUNTIME_SHORT = "r";
   public static final String OPT_RUNTIME = "runtime";

   public static final String OPT_ATTRIBUTE_SHORT = "a";
   public static final String OPT_ATTRIBUTE = "attribute";

   public static final String OPT_CREDENTIALS_SHORT = "d";
   public static final String OPT_CREDENTIALS = "credentials";

   public static final String OPT_HELP_SHORT = "h";
   public static final String OPT_HELP = "help";

   public static final String OPT_COMMAND_SHORT = "c";
   public static final String OPT_COMMAND = "command";

   public static final String OPT_LIST_COMMANDS_SHORT = "lc";
   public static final String OPT_LIST_COMMANDS = "listcommands";

   public static final String OPT_FORCE_SHORT = "f";
   public static final String OPT_FORCE = "force";


   final Options options = new Options();

   JmxScript() {

      options.addOption(OPT_LOGIN_SHORT, OPT_LOGIN, true, "the server access login. Defaults to weblogic");
      options.addOption(OPT_PASSWORD_SHORT, OPT_PASSWORD, true, "the server access password");
      options.addOption(OPT_SERVER_SHORT, OPT_SERVER, true, "the server address");
      options.addOption(OPT_PORT_SHORT, OPT_PORT, true, "the server port to connect to. Defaults to 9080");
      options.addOption(OPT_RUNTIME_SHORT, OPT_RUNTIME, true, "the runtime to query");
      options.addOption(OPT_ATTRIBUTE_SHORT, OPT_ATTRIBUTE, true, "the fetched attribute name");
      options.addOption(OPT_CREDENTIALS_SHORT, OPT_CREDENTIALS, true, "the credentials file that contains the 'login', 'password', 'server' and 'port' for connection");
      options.addOption(OPT_HELP_SHORT, OPT_HELP, false, "Print this help message");
      options.addOption(OPT_COMMAND_SHORT, OPT_COMMAND, true, "send a command to to weblogic. Like an evict cache or whatever.");
      options.addOption(OPT_LIST_COMMANDS_SHORT, OPT_LIST_COMMANDS, false, "List available commands , and exits. (Needs a connection)");
      options.addOption(OPT_FORCE_SHORT, OPT_FORCE, false, "force a cpu consuming jmx command to execute.");

   }

   @SuppressWarnings("unchecked")
   public void execute(String[] args) {
      final CommandLineParser parser = new DefaultParser();

      try {
         final CommandLine commandLine = parser.parse(options, args);

         String runtime = null;
         String login = null;
         String password = null;
         String server = null;
         int port = -1;
         String attribute = null;
         String command = null;
         boolean listCommands = commandLine.hasOption(OPT_LIST_COMMANDS_SHORT);
         boolean force = commandLine.hasOption(OPT_FORCE_SHORT);

         if (commandLine.hasOption(OPT_HELP_SHORT)) {
            printHelp();
            System.exit(EXIT_OK);
         }
         if (commandLine.hasOption(OPT_COMMAND_SHORT)) {
            command = commandLine.getOptionValue(OPT_COMMAND_SHORT);
         }
         final boolean commandDefined = StringUtils.isNotBlank(command);
         if (commandLine.hasOption(OPT_RUNTIME_SHORT)) {
            runtime = commandLine.getOptionValue(OPT_RUNTIME_SHORT);
         } else {
            if (!listCommands && !commandDefined) {
               System.err.println("\n/!\\ Please specify a runtime /!\\\n");
               printHelp();
               System.exit(EXIT_2_NO_RUNTIME);
            }
         }
         if (commandLine.hasOption(OPT_ATTRIBUTE_SHORT)) {
            attribute = commandLine.getOptionValue(OPT_ATTRIBUTE_SHORT);
         } else {
            if (!listCommands && !commandDefined) {
               System.err.println("\n/!\\ Please specify a runtime /!\\\n");
               printHelp();
               System.exit(EXIT_6_MISSING_ATTRIBUTE);
            }
         }

         if (commandLine.hasOption(OPT_CREDENTIALS_SHORT)) {
            try {
               final String[] items = parseCredentialFile(commandLine.getOptionValue(OPT_CREDENTIALS_SHORT));

               login = items[LOGIN_INDEX];
               password = items[PASSWORD_INDEX];
               server = items[SERVER_INDEX];
               try {
                  port = Integer.parseInt(items[PORT_INDEX]);
               } catch (NumberFormatException e) {
                  System.err.print("In credential file, port[" + items[PORT_INDEX] + "] is not parseable as an integer");
                  System.exit(EXIT_7_UNPARSEABLE_PORT_IN_CREDENTIALS);
               }

            } catch (IOException e) {
               e.printStackTrace();
               System.exit(EXIT_3_CREDENTIAL_FILE_PROBLEM);
            }
         } else {
            // get l, p, s, p options

            login = commandLine.getOptionValue(OPT_LOGIN_SHORT, "weblogic");

            final String portString = commandLine.getOptionValue(OPT_PORT_SHORT, "9080");
            try {
               port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
               System.err.print("In credential file, port[" + portString + "] is not parseable as an integer");
               System.exit(EXIT_8_UNPARSEABLE_PORT);
            }

            if (commandLine.hasOption(OPT_PASSWORD_SHORT)) {
               password = commandLine.getOptionValue(OPT_PASSWORD_SHORT);
            } else {
               System.err.println("\n/!\\ Please specify a password /!\\\n");

               System.exit(EXIT_4_MISSING_PASSWORD);
            }

            if (commandLine.hasOption(OPT_SERVER_SHORT)) {
               server = commandLine.getOptionValue(OPT_SERVER_SHORT);
            } else {
               System.err.println("\n/!\\ Please specify a server address/!\\\n");
               System.exit(EXIT_5_MISSING_SERVER);
            }

         }

         // Here all needed variables must have a value.
         assert (runtime != null && !listCommands) || (runtime == null && listCommands);
         assert login != null;
         assert password != null;
         assert server != null;
         assert attribute != null;
         assert port > 0;

         try {
            final JMXServiceURL serviceURL = new JMXServiceURL("t3", server, port, "/jndi/weblogic.management.mbeanservers.runtime");
            Hashtable context = new Hashtable();


            context.put(Context.SECURITY_PRINCIPAL, login);
            context.put(Context.SECURITY_CREDENTIALS, password);
            context.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");

            try {
               final JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, context);
               MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

               final List<JmxPropertyObserverDesc> jmxProperties = getJmxProperties(connection);

               if (listCommands) {
                  for (JmxPropertyObserverDesc jmxProperty : jmxProperties) {
                     System.out.printf("'%s'%s%n", jmxProperty, (StringUtils.isNotBlank(jmxProperty.argument) ? " (can have parameter)" : ""));
                  }
               } else if (commandDefined) {
                  final JmxPropertyObserverDesc selectedCommand = find(command, jmxProperties);
                  if (selectedCommand == null) {
                     System.err.println("Unable to find command '" + command + "'");
                     System.exit(EXIT_21_UNABLE_TON_FIND_COMMAND);
                  }

                  final String[] arguments = commandLine.getArgs();
                  final String[] params = new String[3];

                  params[0] = selectedCommand.className;
                  params[1] = selectedCommand.fieldOrMethodName;
                  try {
                     params[2] = arguments[0];
                  } catch (ArrayIndexOutOfBoundsException e) {
                     // no parameter fallback
                     params[2] = "";
                  }

                  final Object result = connection.invoke(
                        //new ObjectName(String.format("com.bnpparibas.frmk.jmxmonitoring:Location=%s,name=PropertyObserver,type=PropertyObserver", instance))
                        new ObjectName("com.bnpparibas.frmk.jmxmonitoring:name=PropertyObserver,type=PropertyObserver")
                        , "watch"
                        , params
                        , WATCH_METHOD_PARAMS_TYPES);

                  System.out.println(result);
               } else {
                  final Object value = connection.getAttribute(new ObjectName(runtime), attribute);
                  System.out.println(value);
               }

            } catch (IOException e) {
               System.err.println("Connection problem to the server : " + e.getLocalizedMessage());
               System.exit(EXIT_15_JMX_CONNECTION_FAILED);
            } catch (MalformedObjectNameException e) {
               System.err.println("Malformed jmx object name :" + e.getLocalizedMessage());
               System.exit(EXIT_16_MALFORMED_OBJECT);
            } catch (AttributeNotFoundException e) {
               System.err.println("Attribute " + attribute + " not found : " + e.getLocalizedMessage());
               System.exit(EXIT_17_ATTRIBUTE_NOT_FOUND);
            } catch (MBeanException e) {
               System.err.println("Mbean getter problem : " + e.getLocalizedMessage());
               System.exit(EXIT_18_MBEAN_GETTER_EXCEPTION);
            } catch (ReflectionException e) {
               System.err.println("MBean setter problem : " + e.getLocalizedMessage());
               System.exit(EXIT_19_MBEAN_SETTER_EXCEPTION);
            } catch (InstanceNotFoundException e) {
               System.err.println("Instance not found : " + e.getLocalizedMessage());
               System.exit(EXIT_20_INSTANCE_NOT_FOUND);
            }
         } catch (MalformedURLException e) {
            System.err.println("Malformed url : " + e.getLocalizedMessage());
            System.exit(EXIT_9_MALFORMED_JMX_URL);
         }
      } catch (ParseException e) {
         e.printStackTrace();
         System.exit(EXIT_1_UNPARSEABLE_COMMAND_LINE);
      }

   }

   private JmxPropertyObserverDesc find(String command, List<JmxPropertyObserverDesc> jmxProperties) {
      for (JmxPropertyObserverDesc desc : jmxProperties) {
         if (desc.label.equals(command)) {
            return desc;
         }
      }
      return null;
   }

   private List<JmxPropertyObserverDesc> getJmxProperties(MBeanServerConnection connection) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException {
      final String[] properties = (String[]) connection.invoke(new ObjectName("com.bnpparibas.frmk.jmxmonitoring:name=PropertyObserver,type=PropertyObserver"), "retrieveJmxProperties", null, null);
      final List<JmxPropertyObserverDesc> results = new ArrayList<>();
      for (String property : properties) {
         if (!property.startsWith("#")) {
            results.add(new JmxPropertyObserverDesc(property));
         }
      }

      return results;
   }

   /**
    * Reads a credential property file to access connection informations.
    *
    * @param credentialsFile the credential property file.
    * @return [login, password, server, port] list from credentials file.
    * @throws IOException
    */
   private String[] parseCredentialFile(String credentialsFile) throws IOException {
      Properties props = new Properties();
      props.load(new FileInputStream(credentialsFile));

      final String login = props.getProperty("login");
      if (login == null) {
         System.err.println("\n/!\\ Missing login in credential file /!\\\n");
         System.exit(EXIT_10_MISSING_LOGIN_IN_CREDENTIALS);
      }

      final String password = props.getProperty("password");
      if (password == null) {
         System.err.println("\n/!\\ Missing password in credential file /!\\\n");
         System.exit(EXIT_11_MISSING_PASSWORD_IN_CREDENTIALS);
      }
      final String server = props.getProperty("server");
      if (server == null) {
         System.err.println("\n/!\\ Missing server in credential file /!\\\n");
         System.exit(EXIT_12_MISSING_SERVER_IN_CREDENTIALS);
      }
      final String port = props.getProperty("port");
      if (port == null) {
         System.err.println("\n/!\\ Missing port in credential file /!\\\n");
         System.exit(EXIT_13_MISSING_PORT_IN_CREDENTIALS);
      }


      return new String[]{login, password, server, port};

   }

   /**
    * Print the help message to the console.
    */
   private void printHelp() {
      new HelpFormatter().printHelp(120, usage(), "\nThis program allows to fetch jmx data from a weblogic server.\n", options, "\nThe credential file should respect the java properties file format, i.e.\n" +
                  "\ncredential.properties :\n\n" +
                  "login=mylogin\n" +
                  "password=my_super_S3c|_|R3_p4ssW0rd\n" +
                  "server=acetp-prd1-m1.fr.net.intra\n" +
                  "port=9080\n" +
                  "\n" +
                  "Here is the list of possible error exit codes :\n" +
                  exitCodes()
      );
   }

   /**
    * Helper method that returns the list of exit codes.
    *
    * @return
    */
   private String exitCodes() {
      return "  0 ) ok, everything went good.\n" +
            " -1 ) unparseable command line.\n" +
            " -2 ) no runtime.\n" +
            " -3 ) credential file problem.\n" +
            " -4 ) missing password in command line.\n" +
            " -5 ) missing server in command line.\n" +
            " -6 ) missing attribute in command line.\n" +
            " -7 ) unparseable port in credentials.\n" +
            " -8 ) unparseable port in command line.\n" +
            " -9 ) malformed jmx url.\n" +
            "-10 ) missing login in credentials.\n" +
            "-11 ) missing password in credentials.\n" +
            "-12 ) missing server in credentials.\n" +
            "-13 ) missing port in credentials.\n" +
            "-14 ) [removed]\n" +
            "-15 ) jmx connection to the server failed\n" +
            "-16 ) malformed object\n" +
            "-17 ) attribute not found\n" +
            "-18 ) mbean getter exception\n" +
            "-19 ) mbean setter exception\n" +
            "-20 ) instance not found\n";
   }

   /**
    * Print usage command line part.
    *
    * @return
    */
   private String usage() {
      StringWriter sw = new StringWriter();

      PrintWriter pw = new PrintWriter(sw);

      new HelpFormatter().printUsage(pw, 120, "jmxScript", options);
      pw.flush();
      pw.close();
      return sw.toString().substring(USAGE.length());
   }

   public static void main(String[] args) {
      new JmxScript().execute(args);
   }

   private static class JmxPropertyObserverDesc {

      final String className;

      final String fieldOrMethodName;

      final String label;

      final String argument;

      final String possibleArguments;

      final String warningMessage;

      public JmxPropertyObserverDesc(String desc) {
         String[] params = StringUtils.splitPreserveAllTokens(desc, ";");
         this.className = desc == null || params.length == 0 ? null : params[0];
         this.fieldOrMethodName = desc == null || params.length < 2 ? null : params[1];
         this.label = desc == null || params.length < 3 ? null : params[2];
         this.argument = desc == null || params.length < 4 ? null : StringUtils.stripToNull(params[3]);
         this.possibleArguments = desc == null || params.length < 5 ? null : StringUtils.stripToNull(params[4]);
         this.warningMessage = desc == null || params.length < 6 ? null : StringUtils.stripToNull(params[5]);
      }

      @Override
      public String toString() {
         return label;
      }

   }
}
