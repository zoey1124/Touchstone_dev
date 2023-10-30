select
	l_suppkey,
	sum(l_extendedprice * (1 - l_discount))
from
	lineitem
where
	l_shipdate >= date ':1'
	and l_shipdate < date ':1' + interval '3' month
group by
	l_suppkey;
