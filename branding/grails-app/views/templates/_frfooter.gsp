<n:isLoggedIn>
Logged in as <strong><n:principalName /></strong> ( TargetedID: <n:principal /> )<br><br>
</n:isLoggedIn>
Federation Registry <strong>version <g:meta name="app.version"/></strong>
<br>
Developed for the <a href="http://www.aaf.edu.au">Australian Access Federation</a> by <a href="http://bradleybeddoes.com">Bradley Beddoes</a>
<br>
Powered by Grails <g:meta name="app.grails.version"/>

<link rel="stylesheet" href="${resource(dir:'css',file:'zenbox-2.0.css')}" />
<script type="text/javascript" src="${resource(dir: 'js', file: 'zenbox-2.0.js')}"></script>
<script type="text/javascript">
  if (typeof(Zenbox) !== "undefined") {
    Zenbox.init({
      dropboxID:   "6875",
      url:         "australianaccessfederation.zendesk.com",
      tabID:       "support",
      hide_tab:	   true
    });
  }
</script>
