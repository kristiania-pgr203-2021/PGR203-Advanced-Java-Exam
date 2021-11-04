create table Answers
(
    id SERIAL primary key,
    question_id int references Questions(id),
    alternative_id int references Alternatives(id),
    user_id int references Users(id)
);