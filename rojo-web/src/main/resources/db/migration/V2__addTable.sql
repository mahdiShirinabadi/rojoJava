alter table server add column is_json boolean;
alter table server add column country varchar(100);
update server set is_json = false where 1=1;