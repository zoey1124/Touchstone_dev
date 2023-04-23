-- q3
SELECT users.* 
FROM users 
WHERE 
	users.type IN ('User', 'AnonymousUser') 
	AND (LOWER(login) = 'foo') LIMIT $1;
-- q5
SELECT 
	MAX(custom_field_enumerations.position) 
FROM 
	custom_field_enumerations 
WHERE 
	custom_field_enumerations.custom_field_id = $1;

-- q6
SELECT COUNT(*) 
FROM users 
WHERE 
	users.type IN ('User', 'AnonymousUser') 
	AND (users.status <> 0) 
	AND users.status = $1 
	AND (LOWER(users.login) IN (LOWER('%John%')) 
	OR ((
		LOWER(users.firstname) LIKE LOWER('%John%') 
		OR LOWER(users.lastname) LIKE LOWER('%John%'))));

-- q7
SELECT users.* 
FROM users 
WHERE users.status = $1 
	AND users.status = $2
	AND users.type IN ($3, $4)
	AND users.id = $5;