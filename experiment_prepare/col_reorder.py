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

    # reorder the column and write output to another file
    def reorder(input_file, output_file, column_order, column_order_generated):
        data = []
        with open(input_file, 'r') as csv_file:  
            csv_reader = csv.reader(csv_file)
        
            for row in csv_reader:
                data.append(row)

        # Reorder the columns based on the specified order
        reordered_data = []
        for row in data:
            assert(len(row) == len(column_order_generated))
            row_dict = {column_order_generated[i]: row[i] for i in range(len(row))}
            reordered_row = {column: row_dict[column] for column in column_order}
            reordered_data.append(reordered_row)

        # Write the reordered data to a new CSV file
        with open(output_file, 'w', newline='') as csv_file:
            fieldnames = column_order
            csv_writer = csv.DictWriter(csv_file, fieldnames=fieldnames)
            
            for row in reordered_data:
                csv_writer.writerow(row)

    reorder(input_file, output_file, table_to_col_order[table], table_to_col_order_generated[table])

def reorder_all_tables(tables = tables):
    for curr_table in tables:
        to_correct_order(curr_table)

table = "lineitem"
to_correct_order(table)