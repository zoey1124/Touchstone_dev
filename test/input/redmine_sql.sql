-- q0 ok
SELECT  roles.* FROM roles WHERE roles.builtin = $1;

-- q1 ok
SELECT attachments.* FROM attachments WHERE attachments.digest <> $1 AND attachments.filesize <= $2;

-- q2  ok
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

-- q3 ok
SELECT users.* 
FROM users 
WHERE 
	users.type IN ('User', 'AnonymousUser') 
	AND (LOWER(login) = 'foo') LIMIT $1;

-- q4  ok
SELECT 
	custom_values.* 
FROM 
	custom_values 
WHERE 
	custom_values.customized_id = $1 
	AND custom_values.customized_type = $2;


-- q5 ok
SELECT 
	MAX(custom_field_enumerations.position) 
FROM 
	custom_field_enumerations 
WHERE 
	custom_field_enumerations.custom_field_id = $1;

-- q6 ok
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

-- q7 ok
SELECT users.* 
FROM users 
WHERE users.status = $1 
	AND users.status = $2 
	AND users.type IN ($3, $4) 
	AND users.id = $5;

-- q8 ok
SELECT  tokens.* 
FROM tokens 
WHERE tokens.user_id = $1 
AND tokens.action = $2 
AND tokens.value = $3 LIMIT $4;

-- q9 ok
SELECT 1 AS one FROM issues INNER JOIN projects ON projects.id = issues.project_id WHERE (projects.lft < 11 OR projects.rgt > 12) AND issues.fixed_version_id IN ($1, $2) LIMIT $3;



-- q10
SELECT *
FROM issues 
INNER JOIN projects 
	ON projects.id = issues.project_id 
WHERE 
	EXISTS (SELECT 1 AS one FROM enabled_modules WHERE enabled_modules.project_id = projects.id AND enabled_modules.name='issue_tracking')