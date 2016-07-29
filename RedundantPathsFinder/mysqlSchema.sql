create table paths(
id int not null auto_increment,
gateway varchar(1000),
xpath varchar(1000),
runinstance int,
primary key(id)
);

create table hits(
id int not null primary key,
hitcount int
);

Delimiter $$
create procedure sp_cleanup(in instance varchar(100), in instancename varchar(250))
BEGIN
delete from paths where runinstance <> instance and gateway like instancename; 
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