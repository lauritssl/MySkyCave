SkyCave - Starting point for the SkyCave project
====

*Henrik Bærbak Christensen, Aarhus University, 2015*

What is it?
-----------

SkyCave is the exam project in the Cloud Computing and Architecture
course at Computer Science, Aarhus University, 2015.

Please consult material on the course homepage for any further
information.

Requirements
------------

To execute and develop SkyCave, you need Java, Ant, and Ivy installed.

How do I get started?
---------------------

Execute 'ant' to get an overview of most important targets to execute
SkyCave. 

For running the daemon (application server) and the cmd (client/user
interface), review the 'ant daemon' and 'ant cmd' targets.

SkyCave is heavily reconfigurable to allow automated testing, as well
as supporting incremental development work and alternative
implementations of protocols, databases, service connectors, etc. The
configurability is controlled by *environment variables*, all of which
are prefixed by SKYCAVE_. Review the shell scripts (.bat and .sh
files) that are called 'setup...' or 'cfg...' to see examples.

What to do next?
----------------

Solve the exercises posted on Blackboard using the techniques taught
to increase your exam score.