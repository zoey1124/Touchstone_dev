import re
import copy


# a script to convert create sceham sql to touchstone 
app_name = "forem"
input_file_name = "/Users/mengzhusun/Downloads/Touchstone_dev/schema-schedule/app_create/{}.sql".format(app_name)
output_file_name = "/Users/mengzhusun/Downloads/Touchstone_dev/schema-schedule/schema_input/{}.txt".format(app_name)
table_size = 2000

to_data_type = {
                'integer': 'integer',
                'character': 'varchar',
                'timestamp': 'datetime',
                'text': 'varchar',
                'boolean': 'bool',
                'bytea': 'varchar',
                'date': 'date',
                'double': 'decimal',
                'numeric': 'decimal',
                'bigint': 'integer',
                'bytea': 'varchar',
                'tsvector': 'varchar'  # tool does not support for now
                }

class Column:
    def __init__(self, column_name, data_type):
        self.column_name = column_name
        self.data_type = data_type
        self.present = False
    def __str__(self):
        return self.column_name,";", self.data_type

table_name = ""
column_list = []
table_to_columns = {}
with open(input_file_name, 'r') as file:
    line = file.readline()
    while line: 
        line = line.strip().lower()
        if line == "" or line[0] == ")" or line[0] == "-": 
            line = file.readline()
            continue
        if line[-1] == ",":
            line = line[:-1]
        words_arr = re.split(r'\s+', line)
        if words_arr[0] == "create":
            # put last table & columns in map, clear variable names
            if table_name != "":
                table_to_columns[table_name] = copy.deepcopy(column_list)
                column_list = []
            table_name = words_arr[2]
        else: 
            # it is a column
            column_name = words_arr[0]
            raw_column_type = re.sub(r'\(.*', '', words_arr[1])
            data_type = to_data_type[raw_column_type]
            column = Column(column_name, data_type)
            column_list.append(column)

        line = file.readline()

# write to output file in certain format
with open(output_file_name, 'w') as file:
    for table, columns in table_to_columns.items():
        line = "T[{}; {}; ".format(table, table_size)
        for col in columns: 
            line += col.column_name + ", " + col.data_type + "; "
        line += "]"
        print(line)
        file.write(line + '\n')