  <!DOCTYPE html PUBLIC "-//CollabNet//DTD XHTML 1.0 Transitional//EN"
      "http://www.collabnet.com/dtds/collabnet_transitional_10.dtd">

  
    
    
        
  

  <html>
    <head>
      <title>PMD - 
Running PMD on 

<?php 
  print($unixname) 
?>  
    </title>

      <style type="text/css">
        @import url("../style/tigris.css");
        @import url("../style/maven.css");
      </style>
      <script type="text/javascript">
        if (document.layers) {
          document.writeln('<link rel="stylesheet" type="text/css" href="../style/ns4_only.css" media="screen" /><link rel="stylesheet" type="text/css" href="../style/maven_ns4_only.css" media="screen" />')
        }
      </script>
      <link rel="stylesheet" type="text/css" href="../style/print.css" media="print" />

      
    </head>

    <body class="composite" marginwidth="0" marginheight="0">
      <div id="banner">
        <table border="0" cellspacing="0" cellpadding="8" width="100%">
          <tr>
            <td>
              
                
                                
                <a href="http://pmd.sourceforge.net/">
                                  <img src="http://sourceforge.net/sflogo.php?group_id=56262&type=5" align="left" alt="Tom Copeland/David Dixon-Peugh" border="0" />
                                </a>
                
                          </td>
            <td>
              <div align="right" id="login">
                              </div>
            </td>
          </tr>
        </table>
      </div>

      <div id="breadcrumbs">
        <table border="0" cellspacing="0" cellpadding="4" width="100%">
          <tr>
            <td> 
                              <div align="right">
                <a href="http://www.sf.net/projects/pmd">SF Project Page</a>
         |           <a href="http://www.sf.net">Hosted by SourceForge</a>
      </div>

                          </td>
          </tr>
        </table>
      </div>

      <table border="0" cellspacing="0" cellpadding="8" width="100%" id="main">
        <tr valign="top">
          <td id="leftcol" width="20%">
            <div id="navcolumn">

                                <div>
    <strong>Overview</strong>
      <div>
    <small>       <a href="../running.html">Running PMD</a>
   </small>
    
  </div>
  <div>
    <small>       <a href="../ant-task.html">Ant Task</a>
   </small>
    
  </div>
  <div>
    <small>       <a href="../similar-projects.html">Similar Projects</a>
   </small>
    
  </div>
  <div>
    <small>       <a href="../credits.html">Credits</a>
   </small>
    
  </div>

  </div>
  <div>
    <strong>Rule Sets</strong>
      <div>
    <small>       <a href="../rules/basic.html">Basic Rules</a>
   </small>
    
  </div>
  <div>
    <small>       <a href="../rules/naming.html">Naming Rules</a>
   </small>
    
  </div>
  <div>
    <small>       <a href="../rules/unusedcode.html">Unused Code</a>
   </small>
    
  </div>
  <div>
    <small>       <a href="../rules/design.html">Design Patterns</a>
   </small>
    
  </div>

  </div>

                        
                            <div><strong>Project Documentation</strong>
                <div><small><a href="../index.html">Front Page</a></small></div>
                <div>
                  <small><a href="../project-info.html">Project Info</a></small>
                                                                                                                                                                                                        </div>
                <div>
                  <small><a href="../maven-reports.html">Project Reports</a></small>
                                                                                                                                                                                                                                                                                                                                                                                                          </div>
                                <div><small><a href="../apidocs/index.html">JavaDocs</a></small></div>
                <div><small><a href="../xref/index.html">Source XReference</a></small></div>
                                <div><small><a href="http://jakarta.apache.org/turbine/maven/development-process.html">Development Process</a></small></div>
              </div>

            </div>
          </td>
          <td>
            <div id="bodycol">
                            <div class="app">
                  <div class="h3">
    <h3><a name="Running PMD Demo on <?php print($unixname) ?>">Running PMD Demo on <?php print($unixname) ?></a></h3>
    <p>Your PMD Report should be ready in a couple of seconds.  It will
be available from here: 
<?php print("/demo/".$unixname."/pmd.xml") ?>
</p>
<p>
<?php print("/home/groups/p/pm/pmd/tools/demo/run-demo.sh " . $unixname . " " . $module . " " . $source ) ?>
</p>
<p>
<pre>
<?php $cmd = "/home/groups/p/pm/pmd/tools/demo/run-demo.sh " . $unixname . " " . $module . " " . $source . " 2>&1"; 
  print("Executing: " . $cmd);
  print("<br />");

  $fp = popen($cmd, "r" );
  $read = fread( $fp, 4096 );

  print("<br />");
  print( $read );
?>
</pre>
</p>

  </div>

                
                
                
              </div>
            </div>
          </td>
        </tr>
      </table>

      <div id="footer">
        <table border="0" cellspacing="0" cellpadding="4">
          <tr>
            <td>
                                                                  &copy; 2002, Tom Copeland/David Dixon-Peugh
                                                              </td>
          </tr>
        </table>
      </div>

    </body>
  </html>
