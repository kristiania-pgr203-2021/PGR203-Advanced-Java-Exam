create table Alternatives
(
    id SERIAL primary key,
    alternatives varchar(100),
    question_id int references Questions(id)
);