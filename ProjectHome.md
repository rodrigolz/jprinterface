jPrinterface captures text-only output from a legacy business system, perhaps a stock management or epos terminal, by implementing an LPD (line printer demon) server. It can then classify/recognise classes of document, and rewrite them to PDF format. The document can then be printed via CUPS, emailed or archived to disk.

jPrinterface is designed for headless industrial operation. Administration tasks can be performed by a simple embedded webserver.

While this functionality could be (and has been) implemented on Linux using CUPS filters, this project aims to support more complex rewriting, and eg. email output that does not fit as well into the CUPS model