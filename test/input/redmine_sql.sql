-- q1
SELECT  roles.* FROM roles WHERE roles.builtin = $1;

-- q2
SELECT  attachments.* FROM attachments WHERE attachments.digest = $1 AND attachments.filesize = $2;

-- q3
SELECT  auth_sources.* FROM auth_sources WHERE auth_sources.id = $1;

-- q4
SELECT 
	count(users.* )
FROM 
	users 
INNER JOIN 
	email_addresses 
ON 
	email_addresses.user_id = users.id 
WHERE 
	users.type = $1
	AND email_addresses.address = $2;


-- q5 
SELECT 
	count(users.* )
FROM 
	users 
INNER JOIN
	watchers_copy 
ON 
	users.id = watchers_copy.user_id 
WHERE 
	watchers_copy.watchable_type = $1
	AND watchers_copy.watchable_id = $2
	AND users.status = $3;


-- q6
SELECT 
	custom_values.* 
FROM 
	custom_values 
WHERE 
	custom_values.customized_id = $1 
	AND custom_values.customized_type = $2;


-- q7
SELECT 
	MAX(custom_field_enumerations.position) 
FROM 
	custom_field_enumerations 
WHERE 
	custom_field_enumerations.custom_field_id = $1;
