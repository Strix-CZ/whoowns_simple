# About Who-owns

**WARNING** This project is not maintained since 2015.

Who-owns extracts code ownership and authorship information from a version control system. Currently, _git_ and _p4_ are supported.
A code owner is a person or a team who is responsible for an area in the codebase. Who-owns tries to extract this information by
analyzing who is contributing to the area the most and by analyzing keywords used both in the codebase and in commit messages.

## Installation

Make sure you have your version control environment ready. Who-owns uses command line programs `p4` or `git` to access the changelists. 
Install Redis and configure it. These settings are recommended: `save 300 1`, `save 120 10000`, `appendonly no`
Build Who-owns (Ant build file is included)

## Usage
First configure how people are assigned to teams. For the most accurate results also reflect the history of people moving around. Write the information into a text file, with following syntax. Use any string for team names. For people's names use the same username as in version control system.
```
# Who-owns uses it's own syntax. It is really simple, line based - one entry per line.
# Lines starting with a hash are comments.
# Lines with only whitespace are ignored.
    # Whitespace at beginning and end of the line is ignored.

# Create a team by using a keyword "team" followed by a space and team's name:
team RockStars

# Specify a team member by writing his name, followed by a starting date, a keyword `to` and ending date:

jimi.hendrix 1942-11-27 to 1970-09-18

# You can skip ending date which means he is still assigned to the team.
# Be sure to leave the word `to` in place.
neil.young 1945-11-20 to

# You can also skip the starting date which means he was part of the team since beginning of time, or skip both days meaning he was always part of the team:
elvis

# Current limitation is that a person can be assigned at most to a single team in any given time.
team afterlife
jimi.hendrix 1970-09-20 to

# If a person is not assigned to any team (at all or at some point in time) Who-owns assumes they are a team on his own.
# Or you can specify a default team:
default team teamName

# You can ignore all changes done by people assigned to as special team:
ignore changes of team teamName
```

Then create your configuration file, it has similar syntax as team configuration. Who-owns will execute the commands written in this file once it is executed.

```
# There are several available commands:
# clear database
# learn context				(root)
# process changes			("p4"|"git", root directory, branch name, start date, end date)
# extract keywords			(maximal count of keywords)
# extract global keywords	(maximal count of keywords per team)
# extract ownership         ("team"|"author", threshold between 1 and 100, recommended value = 20)

# Start a command with keyword `task`. This command clears the Redis database storing the extracted information.
task clear database
    # this command has no parameters
    
task learn context
    # One line per parameter.
	C:\path\to\project\dir

task extract global keywords
    # Global keywords are not very useful. They are calculated just from IDF-FW value. However you don't have to process any CLs to get them.
	100

# The main task - it goes though the CLs and analyzes them.
task process changes
    # Specifies type of version control system
	git
	C:\path\to\project\dir
	# The branch name (for p4 \\depot\branchName...)
	master
	# Process CLS between the following start date:
	2014-05-01
	# ... and the following end date:
	2015-05-10

# Extracts keywords most suitable for the team.
task extract keywords
	20
	
# Extracts ownership / authorship and groups it based on modules, classes, methods, blocks... This sequence uniquely identifies any piece of code. We call it `ScopePath`.
task extract ownership
    # `author` or `team` to extract individual authorship or team ownership.
   author
   # Threshold - an entity has to be 20% better than the next in order to be an owner.
   20

# Starts "server" communicating through standard output and input
task server
	# Path to root html file.
	web/inlined.html
```

After you have created the configuration file, start Redis and run Who-owns:
`java Main taskfile teamfile localhost:6379`. The last argument is the address and port of Redis.

Who-owns continuously stores the extracted information so you can kill it at any time and it resume where it ended after you start it again. The processing speed is not exactly mind blowing. On medium size test data using git, it analyses about 1 CL per second.

