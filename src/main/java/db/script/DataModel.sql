create table if not exists data_model (
id serial primary key,
name varchar(500),
created timestamp,
text TEXT,
link varchar(1000) UNIQUE
)