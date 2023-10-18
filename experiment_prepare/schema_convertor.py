import re
import copy
import os
import json
from enum import Enum, auto

# a script to convert create sceham sql to touchstone 

app_name = "openproject"
script_directory = os.path.dirname(os.path.abspath(__file__))
app_create_file_name = "{}/app_create/{}.sql".format(script_directory, app_name)
app_constraint_file_name = "{}/app_constraint/{}.txt".format(script_directory, app_name)
output_file_name = "{}/schema_input/{}.txt".format(script_directory, app_name)
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
                'tsvector': 'varchar', # tool does not support for now
                'smallint': 'integer',
                'xml': 'varchar',
                'jsonb': 'varchar'
                }

class Column:
    def __init__(self, column_name: str, data_type: str):
        self.column_name = column_name
        self.data_type = data_type
        self.present = False

    def __str__(self):
        return f"{self.column_name}, {self.data_type}"
    
class Table: 
    def __init__(
            self, 
            name: str, 
            size: int,
            column_list: list[Column]
            ):
        self.name = name
        self.size = size
        self.column_list = column_list
    
    def __str__(self):
        table_info = f"T[{self.name}; {self.size}; "
        for col in self.column_list:
            table_info += f"{str(col)}; "
        table_info += f"p(id)]\n"
        return table_info

class ConstraintType(Enum): 
    INTEGER = auto()
    DECIMAL = auto()
    VARCHAR = auto()
    BOOL = auto()
    DATETIME = auto()
    DATE = auto()


class Constraint:
    def __init__(
            self, 
            table: str, 
            type: ConstraintType, 
            attribute: str,
            null_ratio: float = 0,    
            cardinality: int = 1000,   # Integer
            min_value: int = 1,        # Integer and Decimal 
            max_value: int = 9999,     # Integer and Decimal
            ave_length: float = 15,    # Varchar
            max_length: int = 100,     # Varchar
            values: list[str] = [],    # Inclusion 
            format: str = "",          # Format 
            true_ratio: float = 0.5    # Bool
        ):
        self.table = table
        self.type = type
        self.attribute = attribute
        self.null_ratio = null_ratio
        self.cardinality = cardinality
        self.min_value = min_value
        self.max_value = max_value
        self.ave_length = ave_length
        self.max_length = max_length
        self.values = values
        self.format = format
        self.true_ratio = true_ratio

    def __str__(self):
        line = f"D[{self.table}.{self.attribute}; {self.null_ratio}; "
        match self.type:
            case ConstraintType.INTEGER:
                line += f"{self.cardinality}; {self.min_value}; {self.max_value}"
            case ConstraintType.DECIMAL:
                line += f"{self.min_value}; {self.max_value}"
            case ConstraintType.VARCHAR:
                line += f"{self.ave_length}; {self.max_length}; ({str(self.values)[1: -1]}); {self.format}"
            case ConstraintType.BOOL:
                line += f"{self.true_ratio}"
            case ConstraintType.DATETIME:
                pass
            case ConstraintType.DATE:
                pass
        line += "]\n"
        return line

table_name = ""
column_list = []
table_list = []
with open(app_create_file_name, 'r') as app_create_file:
    line = app_create_file.readline()
    while line: 
        line = line.strip().lower()
        if line == "" or line[0] == ")" or line[0] == "-": 
            line = app_create_file.readline()
            continue
        if line[-1] == ",":
            line = line[:-1]
        words_arr = re.split(r'\s+', line)
        if words_arr[0] == "constraint":
            line = app_create_file.readline()
            continue
        elif words_arr[0] == "create":
            # put last table & columns in map, clear variable names
            if table_name != "":
                table = Table(name=table_name, size=table_size, column_list=copy.deepcopy(column_list))
                table_list.append(table)
                column_list = []
            table_name = words_arr[2]
        else: 
            # it is a column
            column_name = words_arr[0]
            raw_column_type = re.sub(r'\(.*', '', words_arr[1])
            if raw_column_type not in to_data_type:
                continue
            data_type = to_data_type[raw_column_type] 
            column = Column(column_name, data_type)
            column_list.append(column)

        line = app_create_file.readline()

# handle constraints on each columns
constraint_list = []
table_name = ""
with open(app_constraint_file_name) as app_constraint_file:
    constraint_json_list = json.load(app_constraint_file)
    for table_constraint_map in constraint_json_list:
        table_name = table_constraint_map["table"]
        table_constraint_list = table_constraint_map["constraints"]
        for constraint_info in table_constraint_list:
            # Need to consider the case where one attribute has Inclusion & Format Constraint
            match constraint_info['^o']:
                case "InclusionConstraint":
                    attribute_name = constraint_info["field_name"]
                    values = constraint_info["values"]
                    constraint = Constraint(
                        table=table_name, 
                        type=ConstraintType.VARCHAR,
                        attribute=attribute_name,
                        values=values
                    )
                    constraint_list.append(constraint)
                case "FormatConstraint":
                    attribute_name = constraint_info["field_name"]
                    format = constraint_info["format"]
                    constraint = Constraint(
                        table=table_name,
                        type=ConstraintType.VARCHAR,
                        attribute=attribute_name,
                        format=format
                    )

# write to output file in certain format
with open(output_file_name, 'w') as file:
    # write table with columns, primary keys, and foreign keys
    for table in table_list:
        file.write(str(table))

    # write data constraints 
    for constraint in constraint_list:
        file.write(str(constraint))
    