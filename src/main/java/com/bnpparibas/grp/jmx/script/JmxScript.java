package com.bnpparibas.grp.jmx.script;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
import java.util.Hashtable;
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

   private static final int EXIT_OK = 0;
   private static final int EXIT_1_UNPARSEABLE_COMMAND_LINE = -1;
   private static final int EXIT_2_NO_QUERY = -2;
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


   final Options options = new Options();

   JmxScript() {

      options.addOption("l", "login", true, "the server access login. Defaults to weblogic");
      options.addOption("p", "password", true, "the server access password");
      options.addOption("s", "server", true, "the server address");
      options.addOption("o", "port", true, "the server port to connect to. Defaults to 9080");
      options.addOption("q", "query", true, "the query to ask to server");
      options.addOption("a", "attribute", true, "the fetched attribute name");
      options.addOption("c", "credentials", true, "the credentials file that contains the 'login', 'password', 'server'and 'port' for connection");
      options.addOption("h", "help", false, "Print this help message");

   }

   @SuppressWarnings("unchecked")
   public void execute(String[] args) {
      final CommandLineParser parser = new DefaultParser();

      try {
         final CommandLine commandLine = parser.parse(options, args);
         String query = null;
         String login = null;
         String password = null;
         String server = null;
         int port = -1;
         String attribute = null;

         if (commandLine.hasOption("h")) {
            printHelp();
            System.exit(EXIT_OK);
         }
         if (commandLine.hasOption("q")) {
            query = commandLine.getOptionValue("q");
         } else {
            System.err.println("\n/!\\ Please specify a query /!\\\n");
            printHelp();
            System.exit(EXIT_2_NO_QUERY);
         }
         if (commandLine.hasOption("a")) {
            attribute = commandLine.getOptionValue("a");
         } else {
            System.err.println("\n/!\\ Please specify a query /!\\\n");
            printHelp();
            System.exit(EXIT_6_MISSING_ATTRIBUTE);
         }

         if (commandLine.hasOption("c")) {
            try {
               final String[] items = parseCredentialFile(commandLine.getOptionValue("c"));

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

            login = commandLine.getOptionValue("l", "weblogic");

            final String portString = commandLine.getOptionValue("o", "9080");
            try {
               port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
               System.err.print("In credential file, port[" + portString + "] is not parseable as an integer");
               System.exit(EXIT_8_UNPARSEABLE_PORT);
            }

            if (commandLine.hasOption("p")) {
               password = commandLine.getOptionValue("p");
            } else {
               System.err.println("\n/!\\ Please specify a password /!\\\n");

               System.exit(EXIT_4_MISSING_PASSWORD);
            }

            if (commandLine.hasOption("s")) {
               server = commandLine.getOptionValue("s");
            } else {
               System.err.println("\n/!\\ Please specify a server address/!\\\n");
               System.exit(EXIT_5_MISSING_SERVER);
            }

         }

         // Here all needed variables must have a value.
         assert query != null;
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

               final Object value = connection.getAttribute(new ObjectName(query), attribute);
               System.out.println(value);

            } catch (IOException e) {
               System.err.println("Connection problem to the server : " + e.getLocalizedMessage());
               System.exit(EXIT_15_JMX_CONNECTION_FAILED);
            } catch (MalformedObjectNameException e) {
               System.err.println("Malformed jmx object name :" + e.getLocalizedMessage());
               System.exit(EXIT_16_MALFORMED_OBJECT);
            } catch (AttributeNotFoundException e) {
               System.err.println("Attribute "+attribute+" not found : " + e.getLocalizedMessage());
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
    * @return
    */
   private String exitCodes() {
      return "  0 ) ok, everything went good.\n" +
            " -1 ) unparseable command line.\n" +
            " -2 ) no query.\n" +
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
}
