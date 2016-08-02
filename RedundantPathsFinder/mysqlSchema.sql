create table paths(
id int not null auto_increment,
appinstance varchar(250),
xpath varchar(1000),
runinstance int,
primary key(id)
);

create table hits(
id int not null primary key,
hitcount int
);

Delimiter $$
create procedure sp_addentry(in timenow varchar(100), in entry varchar(500), in instance varchar(250))
BEGIN
insert into paths (appinstance, xpath, runinstance) values (timenow, entry, instance); 
END $$
delimiter ;

Delimiter $$
create procedure sp_updateentry(in entry varchar(500), in instance varchar(250))
BEGIN
update paths set runinstance = instance where xpath like entry; 
END $$
delimiter ;

Delimiter $$
create procedure sp_checkremov(in instance varchar(250))
BEGIN
select count(id) from paths where runinstance != instance; 
END $$
delimiter ;

Delimiter $$
create procedure sp_checkentry(in entry varchar(500), in timenow varchar(50))
BEGIN
select id from paths where xpath like entry and appinstance like timenow; 
END $$
delimiter ;

Delimiter $$
create procedure sp_getXpaths(in timenow varchar(50))
BEGIN
select xpath from paths where appinstance like timenow; 
END $$
delimiter ;

Delimiter $$
create procedure sp_cleanup(in instance varchar(100), in timenow varchar(250))
BEGIN
delete from paths where runinstance <> instance and appinstance like timenow; 
END $$
delimiter ;

Delimiter $$
create procedure sp_updatehits(in path varchar(500), in addition int)
BEGIN
declare pathid int  default (select id from paths where xpath like path);
declare currenthits int default (select hitcount from hits where id like pathid);
insert into hits(id, hitcount) values(pathid, 1) on duplicate key update hitcount=(currenthits + addition);
END $$
delimiter ;