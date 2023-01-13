create table IF NOT EXISTS FILMS
(
    FILM_ID      INTEGER auto_increment,
    FILM_NAME    VARCHAR      not null,
    DESCRIPTION  VARCHAR(200) not null,
    RELEASE_DATE DATE                   not null,
    DURATION     VARCHAR      not null,
    RATE        INTEGER,
    constraint FILMS_PK
        primary key (FILM_ID)
);
create table IF NOT EXISTS USERS
(
    USER_ID   INTEGER auto_increment,
    EMAIL     VARCHAR not null
        unique,
    LOGIN     VARCHAR not null
        unique,
    USER_NAME VARCHAR,
    BIRTHDAY  DATE              not null,
    constraint USERS_PK
        primary key (USER_ID)
);
create table IF NOT EXISTS GENRE
(
    GENRE_ID   INTEGER auto_increment
        primary key,
    GENRE_NAME VARCHAR
);
create table IF NOT EXISTS FILM_GENRE
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint FILM_GENRE_PK
        primary key (FILM_ID, GENRE_ID),
    constraint FILM_GENRE_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS
            on delete cascade,
    constraint FILM_GENRE_GENRE_GENRE_ID_FK
        foreign key (GENRE_ID) references GENRE
            on delete cascade
);
create table IF NOT EXISTS CATEGORY
(
    CATEGORY_ID   INTEGER auto_increment
        primary key,
    CATEGORY_NAME VARCHAR
);
create table IF NOT EXISTS FILM_CATEGORY
(
    FILM_ID     INTEGER not null,
    CATEGORY_ID INTEGER,
    constraint FILM_CATEGORY_PK
        primary key (FILM_ID),
    constraint FILM_CATEGORY_CATEGORY_CATEGORY_ID_FK
        foreign key (CATEGORY_ID) references CATEGORY
            on delete cascade,
    constraint FILM_ID
        foreign key (FILM_ID) references FILMS
            on delete cascade
);
create table IF NOT EXISTS LIKED_FILM
(
    USER_ID INTEGER not null,
    FILM_ID INTEGER not null,
    constraint LIKED_FILM_PK
        primary key (USER_ID, FILM_ID),
    constraint LIKED_FILM_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS
            on delete cascade,
    constraint LIKED_FILM_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS
            on delete cascade
);
create table IF NOT EXISTS FRIEND
(
    USER_ID   INTEGER               not null,
    FRIEND_ID INTEGER               not null,
    STATUS    BOOLEAN default FALSE not null,
    constraint FRIEND_PK
        primary key (USER_ID, FRIEND_ID),
    constraint FRIEND_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS
            on delete cascade,
    constraint FRIEND_USERS_USER_ID_FK_2
        foreign key (FRIEND_ID) references USERS
            on delete cascade
);