The goal of the program is to connect to database (in this case Postgres) and manipulate data with own methods in Java. 

All objects in this program are "immutable objects".

To connect to the database, the files in the class should be changed according to your own database server.
For example:
"jdbc:postgresql://localhost:5432/", "postgres", "postgres"
Connection c = DriverManager.getConnection("<url>", "<user>", "<password>");

The driver is installed using building tool Maven and adding the dependencies of postgresql.

persisted=TRUE means the data is available on the database server.
for that COMMIT is necessary. 

To see how the table is created check the "tennis_player_table.sql".

The program provides examples of how the methods work.


