-- q0
SELECT  roles.* FROM roles WHERE roles.builtin = $1;

-- q1
SELECT  attachments.* FROM attachments WHERE attachments.digest <> $1 AND attachments.filesize <= $2;

-- q2
SELECT 
	count(users.* )
FROM 
	users 
INNER JOIN 
	email_addresses 
ON 
	email_addresses.user_id = users.id 
WHERE 
	users.type IN ($1, $2)
	AND email_addresses.address = $3;

-- q3
SELECT users.* 
FROM users 
WHERE 
	users.type IN ('User', 'AnonymousUser') 
	AND (LOWER(login) = 'foo') LIMIT $1;

-- q4
SELECT 
	custom_values.* 
FROM 
	custom_values 
WHERE 
	custom_values.customized_id = $1 
	AND custom_values.customized_type = $2;


-- q5
SELECT 
	MAX(custom_field_enumerations.position) 
FROM 
	custom_field_enumerations 
WHERE 
	custom_field_enumerations.custom_field_id = $1;
