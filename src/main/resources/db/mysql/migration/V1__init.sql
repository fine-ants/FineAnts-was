create table if not exists exchange_rate
(
    code        varchar(255) not null
        primary key,
    create_at   datetime(6)  null,
    modified_at datetime(6)  null,
    base        bit          not null,
    rate        double       not null
);

create table if not exists holiday
(
    id        bigint auto_increment
        primary key,
    base_date date not null,
    is_open   bit  not null,
    constraint UK_kgfa36j3266j5rf7inqglm2d
        unique (base_date)
);

create table if not exists member
(
    id          bigint auto_increment
        primary key,
    create_at   datetime(6)  null,
    modified_at datetime(6)  null,
    email       varchar(255) not null,
    nickname    varchar(255) not null,
    password    varchar(255) null,
    profile_url varchar(255) null,
    provider    varchar(255) not null,
    constraint UK_hh9kg6jti4n1eoiertn2k6qsc
        unique (nickname)
);

create table if not exists fcm_token
(
    id                     bigint auto_increment
        primary key,
    create_at              datetime(6)  null,
    modified_at            datetime(6)  null,
    latest_activation_time datetime(6)  null,
    token                  varchar(255) null,
    member_id              bigint       null,
    constraint token_member_id_unique
        unique (token, member_id),
    constraint FKf1rbjf8lle4r2in6ovkcgl0w8
        foreign key (member_id) references member (id)
);

create table if not exists notification
(
    dtype                        varchar(31)                                                                not null,
    id                           bigint auto_increment
        primary key,
    create_at                    datetime(6)                                                                null,
    modified_at                  datetime(6)                                                                null,
    is_read                      bit                                                                        null,
    link                         varchar(255)                                                               null,
    reference_id                 varchar(255)                                                               null,
    title                        varchar(255)                                                               null,
    type                         enum ('PORTFOLIO_MAX_LOSS', 'PORTFOLIO_TARGET_GAIN', 'STOCK_TARGET_PRICE') null,
    name                         varchar(255)                                                               null,
    portfolio_id                 bigint                                                                     null,
    stock_name                   varchar(255)                                                               null,
    target_price                 decimal(38, 2)                                                             null,
    target_price_notification_id bigint                                                                     null,
    member_id                    bigint                                                                     null,
    constraint FK1xep8o2ge7if6diclyyx53v4q
        foreign key (member_id) references member (id)
);

create table if not exists notification_preference
(
    id                  bigint auto_increment
        primary key,
    create_at           datetime(6) null,
    modified_at         datetime(6) null,
    browser_notify      bit         not null,
    max_loss_notify     bit         not null,
    target_gain_notify  bit         not null,
    target_price_notify bit         not null,
    member_id           bigint      null,
    constraint UK_sug53kin1ir6qq9790uudjs03
        unique (member_id),
    constraint FKpn714rk5pvp6wjlwd77sngm08
        foreign key (member_id) references member (id)
);

create table if not exists portfolio
(
    id                     bigint auto_increment
        primary key,
    create_at              datetime(6)  null,
    modified_at            datetime(6)  null,
    name                   varchar(255) not null,
    securities_firm        varchar(255) not null,
    budget                 decimal(19)  not null,
    maximum_loss           decimal(19)  not null,
    target_gain            decimal(19)  not null,
    maximum_loss_is_active bit          not null,
    target_gain_is_active  bit          not null,
    member_id              bigint       null,
    constraint UKniiw35vyoiwnxtfhs8im0v2a9
        unique (name, member_id),
    constraint FKhkjiiwx38ctlby4yt4y82tua7
        foreign key (member_id) references member (id)
);

create table if not exists portfolio_gain_history
(
    id                bigint auto_increment
        primary key,
    create_at         datetime(6) null,
    modified_at       datetime(6) null,
    cash              decimal(19) not null,
    current_valuation decimal(19) not null,
    daily_gain        decimal(19) not null,
    total_gain        decimal(19) not null,
    portfolio_id      bigint      null,
    constraint portfolio_gain_history_ibfk_1
        foreign key (portfolio_id) references portfolio (id)
);

create table if not exists role
(
    role_id          bigint auto_increment
        primary key,
    role_description varchar(255) null,
    role_name        varchar(255) not null
);

create table if not exists member_role
(
    member_role_id bigint auto_increment
        primary key,
    member_id      bigint null,
    role_role_id   bigint null,
    constraint FK34g7epqlcxqloewku3aoqhhmg
        foreign key (member_id) references member (id),
    constraint FK8ro2tn5n8wkfy1nyjdqxocwpo
        foreign key (role_role_id) references role (role_id)
);

create table if not exists stock
(
    ticker_symbol    varchar(255) not null
        primary key,
    create_at        datetime(6)  null,
    modified_at      datetime(6)  null,
    company_name     varchar(255) null,
    company_name_eng varchar(255) null,
    is_deleted       bit          not null,
    market           varchar(255) null,
    sector           varchar(255) null,
    stock_code       varchar(255) null
);

create table if not exists portfolio_holding
(
    id            bigint auto_increment
        primary key,
    create_at     datetime(6)  null,
    modified_at   datetime(6)  null,
    portfolio_id  bigint       null,
    ticker_symbol varchar(255) null,
    constraint FK3ixur6cv3eqixv9kc01tihm4i
        foreign key (ticker_symbol) references stock (ticker_symbol),
    constraint FK99yckortu2r0bxjltxfvabcbo
        foreign key (portfolio_id) references portfolio (id)
);

create table if not exists purchase_history
(
    id                       bigint auto_increment
        primary key,
    create_at                datetime(6)  null,
    modified_at              datetime(6)  null,
    memo                     varchar(255) null,
    num_shares               decimal(38)  null,
    purchase_date            datetime(6)  null,
    purchase_price_per_share decimal(19)  not null,
    portfolio_holding_id     bigint       null,
    constraint FKtmqhjq2ng9k66gw9s3qbnx0op
        foreign key (portfolio_holding_id) references portfolio_holding (id)
);

create table if not exists stock_dividend
(
    id               bigint auto_increment
        primary key,
    create_at        datetime(6)  null,
    modified_at      datetime(6)  null,
    dividend         decimal(19)  not null,
    ex_dividend_date date         not null,
    payment_date     date         null,
    record_date      date         not null,
    is_deleted       bit          not null,
    ticker_symbol    varchar(255) null,
    constraint UKs7kxldvrap8rcpi7emyaq28y7
        unique (ticker_symbol, record_date),
    constraint FK6tww3epiobccxnj5rgjdu4ab0
        foreign key (ticker_symbol) references stock (ticker_symbol)
);

create table if not exists stock_target_price
(
    id            bigint auto_increment
        primary key,
    create_at     datetime(6)  null,
    modified_at   datetime(6)  null,
    is_active     bit          null,
    member_id     bigint       null,
    ticker_symbol varchar(255) null,
    constraint UKhwlfu5x3iqpei19soxhmjcfs3
        unique (member_id, ticker_symbol),
    constraint FK2r0grp1n205hnw3ysp179f5l3
        foreign key (member_id) references member (id),
    constraint FKcup8hchscft8jniri3wkk72kx
        foreign key (ticker_symbol) references stock (ticker_symbol)
);

create table if not exists target_price_notification
(
    id                    bigint auto_increment
        primary key,
    create_at             datetime(6) null,
    modified_at           datetime(6) null,
    target_price          decimal(19) not null,
    stock_target_price_id bigint      null,
    constraint FKnds69ucw684g4c2a09g0fa5bq
        foreign key (stock_target_price_id) references stock_target_price (id)
);

create table if not exists watch_list
(
    id          bigint auto_increment
        primary key,
    create_at   datetime(6)  null,
    modified_at datetime(6)  null,
    name        varchar(255) null,
    member_id   bigint       null,
    constraint FK913gb7s3b8il5emg0489jhibc
        foreign key (member_id) references member (id)
);

create table if not exists watch_stock
(
    id            bigint auto_increment
        primary key,
    create_at     datetime(6)  null,
    modified_at   datetime(6)  null,
    ticker_symbol varchar(255) null,
    watch_list_id bigint       null,
    constraint FK3eu9b3aw8tnk1lyao7vielolj
        foreign key (watch_list_id) references watch_list (id),
    constraint FKk1yabpeilnrrys4og9yid2cw1
        foreign key (ticker_symbol) references stock (ticker_symbol)
);

