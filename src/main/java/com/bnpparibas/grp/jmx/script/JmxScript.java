package com.bnpparibas.grp.jmx.script;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author morinb.
 */
public class JmxScript {
   public static final String USAGE = "usage: ";
   final Options options = new Options();

   JmxScript() {

      options.addOption("l", "login", true, "the server access login");
      options.addOption("p", "password", true, "the server access password");
      options.addOption("s", "server", true, "the server address");
      options.addOption("p", "port", true, "the server port to connect to");
      options.addOption("n", "name", true, "the server jmx name, usually 'acetp-ENV-INSTANCE' : acetp-prd2-m3");
      options.addOption("q", "query", true, "the query to ask to server");
      options.addOption("c", "credentials", true, "the credentials file that contains the 'login', 'password', 'server', 'port' and 'name' for connection");
      options.addOption("h", "help", false, "Print this help message");


   }

   public void execute(String[] args) {
      printHelp();
   }

   private void printHelp() {
      new HelpFormatter().printHelp(120, usage(), "\nThis program allows to fetch jmx data from a weblogic server.\n", options, "\nThe credential file should respect the java properties file format, i.e.\n" +
            "\ncredential.properties :\n\n" +
            "login=mylogin\n" +
            "password=my_super_S3c|_|R3_p4ssW0rd\n" +
            "server=acetp-prd1-m1.fr.net.intra\n" +
            "port=9080\n" +
            "name=acetp-prd-m1");
   }

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
