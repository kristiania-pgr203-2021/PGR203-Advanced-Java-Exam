create table Alternatives
(
    id          SERIAL primary key,
    question_id int references Questions (id),
    alternative varchar(100)
);