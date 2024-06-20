<h5>API Usage Guide</h5>

<h6>POST - Running the script and adding to the queue</h6>
<p><code>/api/scripts/execute?blocking=false</code></p>
<p>You need to pass the script and parameter:</p>
<ul>
  <li>Parameter <code>blocking=false</code> - indicates that the task is executed asynchronously</li>
  <li>Parameter <code>blocking=true</code> - indicates that the response will be returned when the task is completed</li>
</ul>
<p>Example answer JSON:</p>
<pre>
<code>
{
  "id": "c045bcdd-ba85-47c1-b15e-8ea0df95c325",
  "output": "fdsfd\n",
  "error": "",
  "status": "COMPLETED"
}
</code>
</pre>

<hr>

<h6>GET - Getting all scripts</h6>
<p><code>/api/scripts</code></p>
<p>Parameters:</p>
<ul>
  <li><code>sortedTime</code> - accepts the value:
    <ul>
      <li><code>UPPER</code> - sorts from least to most</li>
      <li><code>LOWER</code> - sorts from largest to smallest</li>
      <li><code>DEFAULT</code> - sorts randomly</li>
    </ul>
  </li>
  <li><code>sortedStatus</code> - accepts the value:
    <ul>
      <li><code>ALL</code> - all statuses</li>
      <li><code>ERROR</code> - sort by ERROR status</li>
      <li><code>COMPLETED</code> - sort by COMPLETED status</li>
      <li><code>PROCESSING</code> - sort by PROCESSING status</li>
      <li><code>STOPPED</code> - sort by STOPPED status</li>
      <li><code>QUEUE</code> - sort by QUEUE status</li>
    </ul>
  </li>
</ul>
<p>Example answer JSON:</p>
<pre>
<code>
[{
  "id": "529fadc8-a2fe-44b4-8e3b-2d87ec18bbb7",
  "script": "console.log('fdsfd')\n",
  "result": {
    "output": "fdsfd\n",
    "error": "",
    "status": "COMPLETED"
  },
  "duration": 653
}]
</code>
</pre>

<hr>

<h6>GET - Get detailed script info</h6>
<p><code>/api/scripts/{id}</code></p>
<p>Example answer JSON:</p>
<pre>
<code>
{
  "id": "529fadc8-a2fe-44b4-8e3b-2d87ec18bbb7",
  "script": "console.log('fdsfd')\n",
  "result": {
    "output": "fdsfd\n",
    "error": "",
    "status": "COMPLETED"
  },
  "startTime": 1718738941161,
  "duration": 653
}
</code>
</pre>

<p>Example answer exception JSON:</p>

<pre>
<code>
{
"statusOperation": "ERROR",
"message": "Script with this id not found",
"id": "529fadc8-a2fe-44b4-8e3b-2d87ec18bbb7"
}
</code>
</pre>

<hr>

<h6>DELETE - Forcibly stop any running or scheduled script</h6>
<p><code>/api/scripts/{id}/stop</code></p>
<p>Example answer JSON:</p>
<pre>
<code>
{
"statusOperation": "OK",
"message": "Script stopped",
"id": "a59f3aa3-6c8a-4433-847d-baf11e3fe45f"
}
</code>
</pre>

<p>Example answer exception JSON:</p>
<pre>
<code>
{
"statusOperation": "ERROR",
"message": "Script with this id not found",
"id": "a59f3aa3-6c8a-4433-847d-baf11e3f45f"
}
</code>
</pre>

<hr>

<h6>DELETE - Remove inactive scripts</h6>
<p><code>/api/scripts/{id}</code></p>
<p>Example answer JSON:</p>
<pre>
<code>
{
"statusOperation": "OK",
"message": "Script deleted",
"id": "a59f3aa3-6c8a-4433-847d-baf11e3fe45f"
}
</code>
</pre>

<p>Example answer exception JSON:</p>
<pre>
<code>
{
"statusOperation": "ERROR",
"message": "Script with this id not found",
"id": "a59f3aa3-6c8a-4433-847d-baf11e3f45f"
}
</code>
</pre>
