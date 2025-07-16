-- Alter Table Bill
alter table bill
add constraint fk_bill_customer foreign key (customer_id) references customer(customer_id);

-- Alter Table Billitem
alter table billitem
add constraint fk_billitem_bill foreign key (bill_id) references bill(bill_id),
add constraint fk_billitem_medicine foreign key (medicine_id) references medicine(medicine_id),
add constraint fk_billitem_customer foreign key (customer_id) references customer(customer_id);