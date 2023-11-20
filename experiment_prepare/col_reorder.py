import csv

'''
a script to correct table column orders to load csv files into database
'''

# correct orders in database
nation_column_order = ["n_nationkey", "n_name", "n_regionkey", "n_comment"]
supplier_column_order = ["s_suppkey", "s_name", "s_address", "s_nationkey", "s_phone", "s_acctbal", "s_comment"]
customer_column_order = ["c_custkey", "c_name", "c_address", "c_nationkey", "c_phone", "c_acctbal", "c_mktsegment", "c_comment"]
partsupp_column_order = ["ps_partkey", "ps_suppkey", "ps_availqty", "ps_supplycost", "ps_comment"]
orders_column_order = ["o_orderkey", "o_custkey", "o_orderstatus", "o_totalprice", "o_orderdate", "o_orderpriority", "o_clerk", "o_shippriority", "o_comment"]
lineitem_column_order = ["l_orderkey", "l_partkey", "l_suppkey", "l_linenumber", "l_quantity", "l_extendedprice", "l_discount", "l_tax", "l_returnflag", "l_linestatus", "l_shipdate", "l_commitdate", "l_receiptdate", "l_shipinstruct", "l_shipmode", "l_comment"]
table_to_col_order = {
    "nation": nation_column_order,
    "supplier": supplier_column_order,
    "customer": customer_column_order,
    "partsupp": partsupp_column_order,
    "orders": orders_column_order,
    "lineitem": lineitem_column_order,
    }

# header in generated file with wrong column order
nation_column_order_generated = ["n_nationkey", "n_regionkey", "n_name", "n_comment"]
supplier_column_order_generated = ["s_suppkey", "s_nationkey", "s_name", "s_address", "s_phone" ,"s_acctbal", "s_comment"]
customer_column_order_generated = ["c_custkey","c_nationkey","c_name","c_address","c_phone","c_acctbal","c_mktsegment","c_comment"]
partsupp_column_order_generated = ["unique_number", "ps_partkey", "ps_suppkey", "ps_availqty", "ps_supplycost", "ps_comment"]
orders_column_order_generated = ["o_orderkey", "o_custkey", "o_orderstatus", "o_totalprice", "o_orderdate", "o_orderpriority", "o_clerk", "o_shippriority", "o_comment"]
lineitem_column_order_generated = ["l_linenumber", "l_partkey", "l_suppkey", "l_orderkey", "l_quantity", "l_extendedprice", "l_discount", "l_tax", "l_returnflag", "l_linestatus", "l_shipdate", "l_commitdate", "l_receiptdate", "l_shipinstruct","l_shipmode","l_comment"]
table_to_col_order_generated = {
    "nation": nation_column_order_generated,
    "supplier": supplier_column_order_generated,
    "customer": customer_column_order_generated,
    "partsupp": partsupp_column_order_generated,
    "orders": orders_column_order_generated,
    "lineitem": lineitem_column_order_generated,
}

tables = ["nation", "supplier", "customer", "partsupp", "orders", "lineitem"]

'''
read in 
insert the first line 
reorder
write out'''
def to_correct_order(table):
    input_file = f"/home/zoey/Touchstone_dev/data/{table}_0.txt"
    output_file = f"/home/zoey/Touchstone_dev/data/{table}_0_out.txt"

    def reorder_save_mem(input_file, output_file, column_order, column_order_generated):
        with open(input_file, 'r') as input_csv_file, open(output_file, 'w') as output_csv_file:
            csv_reader = csv.reader(input_csv_file)
            csv_writer = csv.DictWriter(output_csv_file, fieldnames=column_order)

            for row in csv_reader:
                assert(len(row) == len(column_order_generated))
                modified_row = [0 if elm == "null" else elm for elm in row]
                row_dict = {column_order_generated[i]: modified_row[i] for i in range(len(modified_row))}
                reordered_row = {column: row_dict[column] for column in column_order}
                csv_writer.writerow(reordered_row)

    reorder_save_mem(input_file, output_file, table_to_col_order[table], table_to_col_order_generated[table])

def reorder_all_tables(tables = tables):
    for curr_table in tables:
        to_correct_order(curr_table)


to_correct_order("lineitem")
# reorder_all_tables()

drop_cnt_cmd = "alter table partsupp drop constraint partsupp_pkey cascade;"
dump_to_db_cmd = "COPY lineitem FROM '/home/zoey/Touchstone_dev/data/lineitem_0_out.txt' delimiter ',' CSV;"