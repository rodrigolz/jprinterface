
package sos.net.print;

import java.net.*;
import java.io.*;
import java.util.*;

/** This class provides methods for the Line Printer Daemon Protocol
  * <BR>
  * more infos about LPD/LPR <A HREF=gopher://www-mpl.sri.com/0exec:1179:gopher_root:[mpl.scripts]getrfc.script>RFC 1179</A>
  *
  * @author <A HREF=mailto:supermario@gmx.net>Mario M&uuml;ller</A>
  * @version 1.0 (12/98)
  */

public class LPR {

    private String nextjobid = "000";

    private String host;
    private int port = 515;
    private String user = "nobody";

    private List<String> jobs = new ArrayList<String>();

    private String hostname = null;
    private String jobname = "";

    private String cfAlen;
    private String cfA;

    private int timeout = 60000;

    private boolean cfA_formatted = false;    // f
    private boolean cfA_postscript = false;   // o
    private boolean cfA_DVI = false;          // d
    private boolean cfA_CIF = false;          // c
    private boolean cfA_banner = false;       // L
    private boolean cfA_pr = false;           // p


  /** default constructor without parameters,
    * standard port is <B>515</B>
    */
  public LPR() {

    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (Exception e2) {
      System.out.println("can't resolve hostname");
      hostname = null;
    }
  }


  /** constuctor with host and username
    * standard port is <B>515</B>
    */
  public LPR(String host, String user) {

    setHost(host);
    setUser(user);

    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (Exception e2) {
      System.out.println("can't resolve hostname");
      hostname = null;
    }
  }

  /** constuctor with host, port and username
    */
  public LPR(String host, int port, String user) {

    setHost(host);
    setUser(user);
    setPort(port);

    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (Exception e2) {
      System.out.println("can't resolve hostname");
      hostname = null;
    }
  }


  /** set LPD host
    */
  public void setHost(String value) {
    host = value;
  }

  /** get LPD host
    */
  public String getHost() {
    return host;
  }

  /** set LPD port
    */
  public void setPort(int value) {
    port = value;
  }

  /** get LPD port
    */
  public int getPort() {
    return port;
  }

  /** set username
    */
  public void setUser(String value) {
    user = value;
  }

  /** get username
    */
  public String getUser() {
    return user;
  }

  /** set the timeout for any commands
    */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /** get the timeout for any commands
    */
  public int getTimeout() {
    return timeout;
  }

  /** get if file printed as postscript file
    */
  public boolean get_cfA_postscript() {
    return cfA_postscript; }

  /** set if file printed as postscript file
    * <P><I>o option in control file</I></P>
    * <P>This command prints the data file to be printed, treating the data as
    * standard Postscript input.</P>
    */
  public void set_cfA_postscript(boolean value) {
    cfA_postscript = value;
    if (value) cfA_formatted = false;
  }

  /** get if file printed as plain text
    */
  public boolean get_cfA_formatted() {
    return cfA_formatted; }

  /** set if file printed as plain text
    * <P><I>f option in control file</I></P>
    * </P>This command cause the data file to be printed as a plain text file,
    * providing page breaks as necessary.  Any ASCII control characters
    * which are not in the following list are discarded: HT, CR, FF, LF,
    * and BS.</P>
    */
  public void set_cfA_formatted(boolean value) {
    cfA_formatted = value;
    if (value) cfA_postscript = false;
  }

  /** get if file printed as TeX output
    */
  public boolean get_cfA_DVI() {
    return cfA_DVI; }

  /** set if file printed as TeX output
    * <P><I>d option in control file</I></P>
    * <P>This command causes the data file to be printed, treating the data as
    * DVI (TeX output).</P>
    */
  public void set_cfA_DVI(boolean value) {
    cfA_DVI = value; }

  /** get if file printed as CalTech file
    */
  public boolean get_cfA_CIF() {
    return cfA_CIF; }

  /** set if file printed as CalTech file
    * <P><I>c option in control file</I></P>
    * <P>This command causes the data file to be plotted, treating the data as
    * CIF (CalTech Intermediate Form) graphics language.</P>
    */
  public void set_cfA_CIF(boolean value) {
    cfA_CIF = value; }


  /** get the job name
    */
  public String get_cfA_jobname() {
    return jobname;
  }

  /** set the job name
    * <P><I>J option in control file</I></P>
    * <P>This command sets the job name to be printed on the banner page.  The
    * name of the job must be 99 or fewer octets.  It can be omitted.  The
    * job name is conventionally used to display the name of the file or
    * files which were "printed".  It will be ignored unless the print
    * banner command ('L') is also used.</P>
    */
  public void set_cfA_jobname(String value) {
    jobname = value;
    cfA_banner = true;
  }

  /** get if print file with 'pr' format
    */
  public boolean get_cfA_pr() {
    return cfA_pr;
  }

  /** <P><I>p - Print file with 'pr' format</I></P>
    * <P>This command causes the data file to be printed with a heading, page
    * numbers, and pagination.  The heading should include the date and
    * time that printing was started, the title, and a page number
    * identifier followed by the page number.  The title is the name of
    * file as specified by the 'N' command, unless the 'T' command (title)
    * has been given.  After a page of text has been printed, a new page is
    * started with a new page number.  (There is no way to specify the
    * length of the page.)</P>
    */
  public void set_cfA_pr(boolean value) {
    cfA_pr = value;
  }

  /** get if job printed with banner page
    */
  public boolean get_cfA_banner() {
    return cfA_banner;
  }

  /** set if job printed with banner page
    * <P><I>L option in control file</I></P>
    * <P>This command causes the banner page to be printed.  The user name can
    * be omitted.  The class name for banner page and job name for banner
    * page commands must precede this command in the control file to be
    * effective.</P>
    */
  public void set_cfA_banner(boolean value) {
    cfA_banner = value;
  }

  /** get the host name for this computer
    */
  public String getHostName() {
    return hostname; }

  /** set the host name for this computer
    */
  public void setHostName(String value) {
    hostname = value;
  }

  /** Print any waiting jobs
    * <P>This command starts the printing process if it not already running.</P>
    */
  public boolean printWaitingJobs(String queue) {

      Socket printer = connect();

    if (printer!=null) {

      try {

      DataInputStream in = new DataInputStream(printer.getInputStream());
      DataOutputStream out = new DataOutputStream(printer.getOutputStream());


      //-------------------------- start -------------------------------
        out.write(01);
        out.writeBytes(queue + "\n");

          if (in.readByte() != 0) {
            System.out.println("Error while start print jobs on queue " + queue);
            return false;
          }

        close(printer, in, out);

        return true;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
      return false;
  }

  /** Remove job
    * <P>This command deletes the print jobs from the specified queue which
    * are listed as the other operands.  If only the agent is given, the
    * command is to delete the currently active job.  Unless the agent is
    * "root", it is not possible to delete a job which is not owned by the
    * user.  This is also the case for specifying user names instead of
    * numbers.  That is, agent "root" can delete jobs by user name but no
    * other agents can.</P>
    */
  public boolean removeJob(String queue, String user, String jobid) {

      Socket printer = connect();

    if (printer!=null) {

      try {

        DataInputStream in = new DataInputStream(printer.getInputStream());
        DataOutputStream out = new DataOutputStream(printer.getOutputStream());

      //-------------------------- start -------------------------------
        out.write(05);
        out.writeBytes(queue + " " + user + " " + jobid + "\n");

          if (in.readByte() != 0) {
            System.out.println("Error while remove print job " + jobid + " on queue " + queue);
            return false;
          }

        close(printer, in, out);

        return true;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
      return false;
  }

  /** gets the state and description of the printer queue in short or long format
    */
  public String getQueueState(String queue, boolean shortstate) {

      Socket printer = connect();

    if (printer!=null) {

      try {

        DataInputStream in = new DataInputStream(printer.getInputStream());
        DataOutputStream out = new DataOutputStream(printer.getOutputStream());

        if (shortstate) {
          out.write(03);
        } else {
          out.write(04);
        }
        out.writeBytes(queue + " \n");

        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));

        String line = lnr.readLine();

        close(printer, in, out);

        return line;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
      return null;
  }

  /** print a byte array with the document name as parameter
    * @see LPR#print(String queue, String dfA, String document) {
    */
  public String print(String queue, byte[] dfA, String document) {
      PrintJob job = new PrintJob(queue, dfA, document);
      job.start();
    return job.getJobId();
  }

  /** print a String with the document name as parameter
    * @see LPR#print(String queue, String dfA, String document) {
    */
  public String print(String queue, String dfA, String document) {
      PrintJob job = new PrintJob(queue, dfA.toCharArray(), document);
      job.start();
    return job.getJobId();
  }

  /** print a char array with the document name as parameter
    * @see LPR#print(String queue, String dfA, String document) {
    */
  public String print(String queue, char[] dfA, String document) {
      PrintJob job = new PrintJob(queue, dfA, document);
      job.start();
    return job.getJobId();
  }

  /** print a file with the document name as parameter
    * @see LPR#print(String queue, String dfA, String document) {
    */
  public String print(String queue, File file, String document) {

    if (file.exists()) {

      try {

        FileInputStream in = new FileInputStream(file);

        byte[] data = new byte[(int)file.length()];
        int count = 0;
        int size = (int)file.length();

        while (count < size) {
          count += in.read(data, count, size-count);
        }

        in.close();

        PrintJob job = new PrintJob(queue, data, document);
        job.start();

        return job.getJobId();

      } catch (Exception e) {
        System.out.println("error while printig file " + file.getName() + "\n" + e.getMessage());
      }
    } else {
      System.out.println("file " + file.getName() + " not found");
    }
      return null;
  }

  /** wait until job is printed
    */
  public void waitFor(String jobid) {
    try {
      while (jobs.contains(jobid)) {
        Thread.sleep(500);
      }
    } catch (Exception e) { }
  }


  /** internal funktion for printing
    */
  private boolean print(String queue, String document, byte[] dfA1, char[] dfA2, int mode, String jobid) {

      Socket printer = connect();

    if (printer!=null) {

      boolean rc = true;

        makecfA(document, jobid);

        String dfAlen = String.valueOf( mode==1 ? dfA1.length : dfA2.length );

      try {

        DataInputStream in = new DataInputStream(printer.getInputStream());
        DataOutputStream out = new DataOutputStream(printer.getOutputStream());

        System.out.println("jobid: " + jobid + " queue: " + queue);

    //-------------------------- start -------------------------------
        out.write(02);
        out.writeBytes(queue + "\n");

          if (in.readByte() != 0) {
            rc = false;
            System.out.println("Error while start printing on queue " + queue);
          }

    //----------------------- write data file ----------------------------
        out.write(03);
        out.writeBytes(dfAlen);
        out.writeBytes(" ");
        out.writeBytes("dfA" + jobid + user + "\n");

          if (in.readByte() != 0) {
            rc = false;
            System.out.println("Error while start sending data file");
          }

        if (mode == 1) {
          out.write(dfA1);
        } else {
          out.writeBytes(new String(dfA2));
        }
        out.writeByte(0);

          if (in.readByte() != 0) {
            rc = false;
            System.out.println("Error while sending data file");
          }

    //----------------------- write control file ----------------------------

        out.write(02);
        out.writeBytes(cfAlen);
        out.writeBytes(" ");
        out.writeBytes("cfA" + jobid + user + "\n");

          if (in.readByte() != 0) {
            rc = false;
            System.out.println("Error while start sending control file");
          }

        out.writeBytes(cfA);
        out.writeByte(0);

          if (in.readByte() != 0) {
            rc = false;
            System.out.println("Error while sending control file");
          }

        out.write(01);

        out.flush();

        close(printer, in , out);

      } catch (Exception e) {
        rc = false;
        e.printStackTrace();
      }
        return rc;
    }
    return false;
  }

  /** connect to the LPD Server
    */
  private Socket connect() {
    try {
      System.out.println("Connect with " + host);

      Socket socket = new Socket(InetAddress.getByName(host), port);

      socket.setSoTimeout(timeout);

      return socket;

    } catch (Exception e1) {
      System.out.println("Error while connecting to " + host + ":" + port);
      System.out.println(e1.getMessage());
    }
      return null;
  }

  /** closed connection to the LPD
    */
  private void close(Socket socket, DataInputStream in, DataOutputStream out) {
      try {

        in.close();
        out.close();

        socket.close();

      } catch (Exception e) {
        System.out.println("Errror while closing printerport");
        System.out.println(e.getMessage());
      }
  }

  /** internal methode to create the control file
    */
  private void makecfA(String document, String jobid) {

    cfA = "";

    if (hostname!=null) cfA += "H" + hostname +"\n";

    cfA += "J" + document + "\n";
    cfA += "P" + user + "\n";

    if (cfA_formatted) cfA += "f" + document + "\n";
    if (cfA_postscript) cfA += "o" + document + "\n";
    if (cfA_CIF) cfA += "c" + document + "\n";
    if (cfA_DVI) cfA += "d" + document + "\n";
    if (cfA_banner) cfA += "L" + jobname + "\n";

    cfA += "ldfA" + jobid + user +"\n";

    cfAlen = String.valueOf(cfA.length());

//    System.out.println(cfA);
  }


  private String getNewJobId() {
    return fillLeft(String.valueOf(Integer.parseInt(nextjobid) + 1), 3, "0");
  }


  private String fillLeft(String data, int size, String filler) {
    while (data.length() < size) {
      data = filler + data;
    }
    return data;
  }

//  private String fillRight(String data, int size, String filler) {
//    while (data.length() < size) {
//      data += filler;
//    }
//    return data;
//  }

  /********************* Job *************************/
  private class PrintJob extends Thread {

      String queue;
      String document;
      String id;
      int mode;
      private byte[] dfA1 = null;
      private char[] dfA2 = null;

    public PrintJob(String queue, byte[] data, String document) {
      super();
        this.queue = queue;
        this.dfA1 = data;
        this.document = document;
        this.mode = 1;

        this.id = getNewJobId();

        jobs.add(id);
    }

    public PrintJob(String queue, char[] data, String document) {
      super();
        this.queue = queue;
        this.dfA2 = data;
        this.document = document;
        this.mode = 2;

        this.id = getNewJobId();

        jobs.add(id);
    }

    public void run() {
        boolean rc = false;
      try {
        rc = print(queue, document, dfA1, dfA2, mode, id);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
        jobs.remove(id);
      if (rc) {
        System.out.println("Job " + id + " allready printed and removed from queue");
      } else {
        System.out.println("Job " + id + " not printed and removed from queue");
      }
    }

    public String getJobId() {
      return this.id;
    }

  }


}