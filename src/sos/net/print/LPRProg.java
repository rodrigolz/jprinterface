package sos.net.print;


//Titel:Line Printer Daemon Protocol
//Version:
//Copyright:Copyright (c) 1998
//Autor:Mario Mueller
//Organisation:Shamrock-Online
//Beschreibung:

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import sos.net.print.*;


public class LPRProg extends Frame {

  LPR lpr = null;

  String[] filetypes = {"Formatted", "No Filter", "Page Numbers", "Postscript"};

  MenuBar PRINT = new MenuBar();
  Menu menu1 = new Menu();
  MenuItem menuItem1 = new MenuItem();
  MenuItem menuItem2 = new MenuItem();
  MenuItem menuItem3 = new MenuItem();
  MenuItem menuItem4 = new MenuItem();
  MenuItem menuItem5 = new MenuItem();
  Panel panel1 = new Panel();
  Button button1 = new Button();
  Button printBtn = new Button();
  Button stateBtn = new Button();
  TextField textQueue = new TextField();
  TextField textHost = new TextField();
  TextField textUser = new TextField();
  TextField textPort = new TextField();
  Checkbox portCheck = new Checkbox();
  Label label1 = new Label();
  Label label2 = new Label();
  Label label3 = new Label();
  Checkbox bannerCheck = new Checkbox();
  List filetypeList = new List();
  Label label4 = new Label();
  TextField textTimeout = new TextField();
  Label label5 = new Label();
  List listState = new List();


  public LPRProg() {
    try  {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }

    for (int i=0; i<filetypes.length; i++) {
      filetypeList.addItem(filetypes[i]);
    }
    filetypeList.select(1);

      setVisible(true);
  }

  public static void main(String[] args) {
    LPRProg prog = new LPRProg();
  }

  private void jbInit() throws Exception {
    this.setMenuBar(PRINT);
    this.setSize(new Dimension(543, 405));
    this.setTitle("LPR - Line Printer Requester");
    menu1.setLabel("LPR");
    menuItem1.setLabel("Print..");
    menuItem1.setActionCommand("PRINT");
    menuItem2.setLabel("Resume Queue");
    menuItem2.setActionCommand("RESUME");
    menuItem3.setLabel("Remove Job");
    menuItem3.setActionCommand("REMOVE");
    menuItem4.setLabel("Refresh");
    menuItem4.setActionCommand("REFRESH");
    menuItem5.setLabel("Exit");
    menuItem5.setActionCommand("EXIT");
    panel1.setBackground(Color.lightGray);
    button1.setBounds(new Rectangle(22, 259, 98, 23));
    button1.setActionCommand("connectBtn");
    button1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        button1_actionPerformed(e);
      }
    });
    printBtn.setLabel("Print..");
    printBtn.setActionCommand("connectBtn");
    printBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        printBtn_actionPerformed(e);
      }
    });
    stateBtn.setLabel("get State");
    stateBtn.setActionCommand("connectBtn");
    stateBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stateBtn_actionPerformed(e);
      }
    });
    textQueue.setBounds(new Rectangle(22, 42, 273, 22));
    textPort.setText("515");
    portCheck.setLabel("User defined printer port number");
    portCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        portCheck_itemStateChanged(e);
      }
    });
    label1.setBounds(new Rectangle(24, 25, 200, 20));
    label1.setText("Queue Name:");
    label2.setText("Host Name or address:");
    label3.setText("User Name for banner page:");
    bannerCheck.setBounds(new Rectangle(336, 139, 154, 23));
    bannerCheck.setLabel("Print banner page");
    filetypeList.setEnabled(false);
    filetypeList.setBounds(new Rectangle(334, 42, 179, 74));
    label4.setText("File Type:");
    textTimeout.setBounds(new Rectangle(145, 213, 72, 22));
    textTimeout.setText("60");
    label5.setText("Timeout (sec):");
    listState.setBounds(new Rectangle(144, 259, 370, 99));
    label5.setBounds(new Rectangle(42, 212, 96, 23));
    label4.setBounds(new Rectangle(338, 23, 66, 23));
    label3.setBounds(new Rectangle(25, 121, 200, 23));
    label2.setBounds(new Rectangle(26, 73, 200, 23));
    textHost.setBounds(new Rectangle(23, 92, 273, 22));
    portCheck.setBounds(new Rectangle(22, 178, 205, 23));
    textPort.setBounds(new Rectangle(247, 179, 48, 23));
    textPort.setEnabled(false);
    textUser.setBounds(new Rectangle(22, 140, 274, 22));
    stateBtn.setEnabled(false);
    stateBtn.setBounds(new Rectangle(22, 335, 98, 23));
    printBtn.setEnabled(false);
    printBtn.setBounds(new Rectangle(22, 303, 98, 23));
    button1.setLabel("Connect");
    panel1.setLayout(null);
//    printBtn.setLabel("");
    menuItem5.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        menuItem5_actionPerformed(e);
      }
    });
    PRINT.add(menu1);
    menu1.add(menuItem1);
    menu1.addSeparator();
    menu1.add(menuItem2);
    menu1.add(menuItem3);
    menu1.add(menuItem4);
    menu1.addSeparator();
    menu1.add(menuItem5);
    this.add(panel1, BorderLayout.CENTER);
    panel1.add(button1, null);
    panel1.add(printBtn, null);
    panel1.add(stateBtn, null);
    panel1.add(textQueue, null);
    panel1.add(textHost, null);
    panel1.add(textUser, null);
    panel1.add(textPort, null);
    panel1.add(portCheck, null);
    panel1.add(label1, null);
    panel1.add(label2, null);
    panel1.add(label3, null);
    panel1.add(bannerCheck, null);
    panel1.add(filetypeList, null);
    panel1.add(label4, null);
    panel1.add(textTimeout, null);
    panel1.add(label5, null);
    panel1.add(listState, null);
    this.setMenuBar(PRINT);
  }

  void menuItem5_actionPerformed(ActionEvent e) {
    System.exit(0);
  }

  void portCheck_itemStateChanged(ItemEvent e) {
    textPort.setEnabled(portCheck.getState());
  }

  void printBtn_actionPerformed(ActionEvent e) {
    FileDialog dlg = new FileDialog(this, "Select file to print", FileDialog.LOAD);

    dlg.show();

    if (dlg.getFile()!=null) {

      File file = new File(dlg.getDirectory() + File.separatorChar + dlg.getFile());

      if (file.exists()) {
        System.out.println("print " + dlg.getDirectory() + dlg.getFile() + " on " + textHost.getText());

        lpr.set_cfA_banner(bannerCheck.getState());
        if (bannerCheck.getState()) lpr.set_cfA_jobname(textUser.getText());

        int type = filetypeList.getSelectedIndex();

        lpr.set_cfA_formatted(type==0);
        lpr.set_cfA_pr(type==2);
        lpr.set_cfA_postscript(type==3);

        try {

          lpr.print(textQueue.getText(), file, dlg.getFile());

          listState.addItem(file.getName() + " on " + textQueue.getText() + "@" + textHost.getText() + " printed");

        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  void button1_actionPerformed(ActionEvent e) {

      String host = textHost.getText();
      int port = 515;
      String user = textUser.getText();

    if (portCheck.getState()) port = Integer.parseInt(textPort.getText());

    lpr = new LPR(host, port, user);

      lpr.setTimeout(Integer.parseInt(textTimeout.getText()) * 1000);

    printBtn.setEnabled(true);
    stateBtn.setEnabled(true);
    filetypeList.setEnabled(true);

  }

  void stateBtn_actionPerformed(ActionEvent e) {
    listState.addItem(lpr.getQueueState(textQueue.getText(), false));
  }
  
}

