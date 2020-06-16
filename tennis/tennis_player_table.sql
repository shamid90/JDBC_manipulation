DROP TABLE IF EXISTS tennis_player;

DROP SEQUENCE IF EXISTS player_id_seq;
CREATE SEQUENCE player_id_seq;


CREATE TABLE tennis_player(
  player_id integer NOT NULL DEFAULT nextval('player_id_seq') PRIMARY KEY,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  last_update TIMESTAMP NOT NULL DEFAULT Now()
);

INSERT INTO tennis_player(first_name, last_name) VALUES ('Roger', 'Federer');
INSERT INTO tennis_player(first_name, last_name) VALUES ('Rafael', 'Nadal');
INSERT INTO tennis_player(first_name, last_name) VALUES ('Novak', 'Djokovic');
INSERT INTO tennis_player(first_name, last_name) VALUES ('Serena', 'Williams');
INSERT INTO tennis_player(first_name, last_name) VALUES ('Maria', 'Sharapova');
INSERT INTO tennis_player(first_name, last_name) VALUES ('Steffi', 'Graf');
