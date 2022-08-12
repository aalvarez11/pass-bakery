# Pass-Bakery
Scala training project

### Project Dependencies
* play json
* jdbc (used by Scala in Play Framework)
* evolutions
* postgres
* doobie

### Setting Up The Database
To create the database that is used in this project, you must have [Docker](https://www.docker.com/products/docker-desktop/) and  [postgresql](https://www.postgresql.org/download/) installed locally on your machine.

Start with launching Docker. Once Docker is running, run the following command to download the postgres alpine image: `docker pull postgres:alpine`

The next step is to start a postgres instance using the command `docker run --name pass-bakery-postgres -e POSTGRES_USER=user -e POSTGRES_PASSWORD=pass -p 5432:5432 -d postgres:alpine` This creates a docker container named pass-bakery-postgres and a postgresql database that will be exposed on port 5432 with the credentials provided: username 'user' and password 'pass'.

To view and access the database in the terminal using bash, use the command `docker exec -it pass-bakery-postgres bash` from there use the bash command `psql -U user` If prompted for the password, provide `pass` or whatever password you made during the `docker run` command.

Create the database used in this project with psql command `create database pass_bakery_db` You can check your local databases using `\l` to make sure pass_bakery_db now exists in your databases. Next, you can connect to it with `\c pass_bakery_db` and you are now able to execute sql in the terminal, like making tables and populating them with records preemptively or for testing.

To log back into the db at any time, run this command in the terminal: `psql -h localhost -p 5432 -U user` afterwards you will be prompted for the password, and it is simply `pass` or any custom password you used in setup.
