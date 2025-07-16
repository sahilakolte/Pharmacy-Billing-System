create database if not exists medicaldb;
use medicaldb;

-- Creating Medicine Table
create table medicine (
	medicine_id int auto_increment,
    medicine_name varchar(30) not null,
    price double not null,
    quantity int not null,
    company varchar(30) not null,
    mfg_date date not null,
    exp_date date not null,
    constraint chk_mfg_exp check (mfg_date < exp_date),
    constraint pk_medicine primary key (medicine_id)
);

-- Creating Customer Table 
create table customer (
	customer_id varchar(4) not null,
    customer_name varchar(30) not null,
    customer_phone char(10) not null,
    constraint pk_customer primary key (customer_id)
);

-- Creating Admin Table 
create table admin (
	admin_id varchar(4) not null,
    password varchar(5) not null,
    admin_name varchar(30) not null,
    admin_phone char(10) unique not null,
    constraint pk_admin primary key (admin_id)
);

-- Creating Bill Table 
create table bill (
	bill_id int auto_increment,
    customer_id varchar(4) not null,
    bill_amount double not null,
    bill_date date not null,
    constraint pk_bill primary key (bill_id)
);

-- Creating Billitem Table
create table billitem (
	bill_id int not null,
    medicine_id int not null,
    quantity int not null,
    customer_id varchar(4) not null,
    constraint pk_billitem primary key (bill_id,medicine_id)
);