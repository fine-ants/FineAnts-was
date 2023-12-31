create table if not exists fineAnts.hibernate_sequence
(
    next_val bigint null
);

create table if not exists fineAnts.member
(
    id          bigint auto_increment
        primary key,
    create_at   datetime     null,
    modified_at datetime     null,
    email       varchar(255) null,
    nickname    varchar(255) null,
    password    varchar(255) null,
    profile_url varchar(255) null,
    provider    varchar(255) null
);

create table if not exists fineAnts.portfolio
(
    id                    bigint auto_increment
        primary key,
    create_at             datetime     null,
    modified_at           datetime     null,
    budget                bigint       null,
    maximum_is_active     bit          null,
    maximum_loss          bigint       null,
    name                  varchar(255) null,
    securities_firm       varchar(255) null,
    target_gain           bigint       null,
    target_gain_is_active bit          null,
    member_id             bigint       null,
    constraint FKhkjiiwx38ctlby4yt4y82tua7
        foreign key (member_id) references fineAnts.member (id)
);

create table if not exists fineAnts.portfolio_gain_history
(
    id                bigint auto_increment
        primary key,
    create_at         datetime null,
    modified_at       datetime null,
    cash bigint null,
    current_valuation bigint   null,
    daily_gain        bigint   null,
    total_gain        bigint   null,
    portfolio_id      bigint   null,
    constraint FKdea3l34fqo27x2xrfdt75otys
        foreign key (portfolio_id) references fineAnts.portfolio (id)
);

create table if not exists fineAnts.stock
(
    ticker_symbol    varchar(255) not null
        primary key,
    create_at        datetime     null,
    modified_at      datetime     null,
    company_name     varchar(255) null,
    company_name_eng varchar(255) null,
    market           varchar(255) null,
    sector           varchar(255) null,
    stock_code       varchar(255) null
);

create table if not exists fineAnts.portfolio_holding
(
    id            bigint auto_increment
        primary key,
    create_at     datetime     null,
    modified_at   datetime     null,
    portfolio_id  bigint       null,
    ticker_symbol varchar(255) null,
    constraint FK3ixur6cv3eqixv9kc01tihm4i
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol),
    constraint FK99yckortu2r0bxjltxfvabcbo
        foreign key (portfolio_id) references fineAnts.portfolio (id)
);

create table if not exists fineAnts.purchase_history
(
    id                       bigint auto_increment
        primary key,
    create_at                datetime     null,
    modified_at              datetime     null,
    memo                     varchar(255) null,
    num_shares               bigint       null,
    purchase_date            datetime     null,
    purchase_price_per_share double       null,
    portfolio_holding_id     bigint       null,
    constraint FKtmqhjq2ng9k66gw9s3qbnx0op
        foreign key (portfolio_holding_id) references fineAnts.portfolio_holding (id)
);

create table if not exists fineAnts.stock_dividend
(
    id               bigint auto_increment
        primary key,
    create_at        datetime     null,
    modified_at      datetime     null,
    dividend         bigint       null,
    ex_dividend_date date         not null,
    payment_date     date         null,
    record_date      date         not null,
    ticker_symbol    varchar(255) null,
    constraint UK9arb4d9tfcndvi89oppsv3wsa
        unique (ticker_symbol, record_date),
    constraint FK6tww3epiobccxnj5rgjdu4ab0
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol)
);

create table if not exists fineAnts.watch_list
(
    id          bigint       not null
        primary key,
    create_at   datetime     null,
    modified_at datetime     null,
    name        varchar(255) null,
    member_id   bigint       null,
    constraint FK913gb7s3b8il5emg0489jhibc
        foreign key (member_id) references fineAnts.member (id)
);

create table if not exists fineAnts.watch_stock
(
    id            bigint       not null
        primary key,
    create_at     datetime     null,
    modified_at   datetime     null,
    ticker_symbol varchar(255) null,
    watch_list_id bigint       null,
    constraint FK3eu9b3aw8tnk1lyao7vielolj
        foreign key (watch_list_id) references fineAnts.watch_list (id),
    constraint FKk1yabpeilnrrys4og9yid2cw1
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol)
);
