create table Questions
(
    id SERIAL PRIMARY KEY,
    question varchar(100),
    survey_id int references Surveys(id)
);