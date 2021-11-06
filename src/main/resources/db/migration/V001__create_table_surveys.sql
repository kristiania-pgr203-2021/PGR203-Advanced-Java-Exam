create table Surveys
(
    id SERIAL PRIMARY KEY,
    survey_name varchar(100)

);

insert into surveys (survey_name) values ('Color blindness');