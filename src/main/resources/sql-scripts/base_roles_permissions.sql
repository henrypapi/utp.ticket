INSERT INTO limondbv2.tbl_role (id, name) VALUES (4, 'ACCESS_CONTROL');
INSERT INTO limondbv2.tbl_role (id, name) VALUES (1, 'ADMIN');
INSERT INTO limondbv2.tbl_role (id, name) VALUES (5, 'PROMOTER_UNQ');
INSERT INTO limondbv2.tbl_role (id, name) VALUES (3, 'PROMOTERS');
INSERT INTO limondbv2.tbl_role (id, name) VALUES (2, 'SALES');

INSERT INTO limondbv2.tbl_role_permission (role_id, permission_id) VALUES (1, 1);
INSERT INTO limondbv2.tbl_role_permission (role_id, permission_id) VALUES (1, 2);
INSERT INTO limondbv2.tbl_role_permission (role_id, permission_id) VALUES (1, 3);
INSERT INTO limondbv2.tbl_role_permission (role_id, permission_id) VALUES (2, 1);
INSERT INTO limondbv2.tbl_role_permission (role_id, permission_id) VALUES (3, 2);
INSERT INTO limondbv2.tbl_role_permission (role_id, permission_id) VALUES (4, 3);
INSERT INTO limondbv2.tbl_role_permission (role_id, permission_id) VALUES (5, 2);

INSERT INTO limondbv2.tbl_permission (id, module_code, description) VALUES (1, 'sales', 'Modulo Ventas');
INSERT INTO limondbv2.tbl_permission (id, module_code, description) VALUES (2, 'promoters', 'Modulo Promotores');
INSERT INTO limondbv2.tbl_permission (id, module_code, description) VALUES (3, 'access_control', 'Modulo Control de Acceso');
