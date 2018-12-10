# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table config (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  device_name                   varchar(255),
  device_mac_address            varchar(255),
  device_id                     varchar(36),
  place_id                      varchar(36),
  organization_id               varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint uq_config_device_id unique (device_id),
  constraint uq_config_place_id unique (place_id),
  constraint uq_config_organization_id unique (organization_id),
  constraint pk_config primary key (id)
);

create table data_migration (
  id                            varchar(255) not null,
  name                          varchar(255),
  description                   varchar(255),
  meta_data                     varchar(255),
  start_date                    datetime(6),
  completed_date                datetime(6),
  migration_state               integer,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint ck_data_migration_migration_state check ( migration_state in (0,1)),
  constraint pk_data_migration primary key (id)
);

create table device (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  external_id                   varchar(255),
  mac_address                   varchar(255),
  owner_id                      varchar(36),
  manufacturer_id               varchar(36),
  current_place_id              varchar(36),
  name                          varchar(128) not null,
  device_secret                 varchar(255),
  device_type_id                varchar(36),
  enabled                       tinyint(1) not null,
  version_number                varchar(64),
  model                         varchar(64),
  software_id                   varchar(36),
  currentbattery_level          varchar(255),
  maximum_battery_level         varchar(255),
  current_state_id              varchar(255),
  current_heartbeat_id          varchar(255),
  oui_company_name              varchar(255),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint uq_device_current_place_id unique (current_place_id),
  constraint uq_device_organization_id_id unique (organization_id,id),
  constraint pk_device primary key (id)
);

create table device_place (
  device_id                     varchar(36) not null,
  place_id                      varchar(36) not null,
  constraint pk_device_place primary key (device_id,place_id)
);

create table device_collaborator_organizations (
  device_id                     varchar(36) not null,
  collaborator_organization_id  varchar(36) not null,
  constraint pk_device_collaborator_organizations primary key (device_id,collaborator_organization_id)
);

create table device_collaborator_users (
  device_id                     varchar(36) not null,
  user_id                       varchar(36) not null,
  constraint pk_device_collaborator_users primary key (device_id,user_id)
);

create table device_type (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  name                          varchar(255) not null,
  description                   varchar(255),
  label_class                   varchar(255),
  image_id                      varchar(36),
  visibility                    integer not null,
  html_id                       varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint ck_device_type_visibility check ( visibility in (0,1,2,3)),
  constraint uq_device_type_image_id unique (image_id),
  constraint uq_device_type_organization_id_name unique (organization_id,name),
  constraint pk_device_type primary key (id)
);

create table device_type_properties (
  device_type_id                varchar(36) not null,
  device_type_property_id       varchar(36) not null,
  constraint pk_device_type_properties primary key (device_type_id,device_type_property_id)
);

create table device_type_property (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  name                          varchar(255) not null,
  description                   varchar(255),
  label_class                   varchar(255),
  image_id                      varchar(36),
  visibility                    integer not null,
  device_type_property_type_id  varchar(36),
  device_type_property_status_id varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint ck_device_type_property_visibility check ( visibility in (0,1,2,3)),
  constraint uq_device_type_property_image_id unique (image_id),
  constraint uq_device_type_property_organization_id_name unique (organization_id,name),
  constraint pk_device_type_property primary key (id)
);

create table device_type_property_status (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  name                          varchar(255) not null,
  description                   varchar(255),
  label_class                   varchar(255),
  element_class                 varchar(255),
  image_id                      varchar(36),
  visibility                    integer not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint ck_device_type_property_status_visibility check ( visibility in (0,1,2,3)),
  constraint uq_device_type_property_status_image_id unique (image_id),
  constraint uq_device_type_property_status_organization_id_name unique (organization_id,name),
  constraint pk_device_type_property_status primary key (id)
);

create table device_type_property_type (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  name                          varchar(255) not null,
  description                   varchar(255),
  label_class                   varchar(255),
  image_id                      varchar(36),
  visibility                    integer not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint ck_device_type_property_type_visibility check ( visibility in (0,1,2,3)),
  constraint uq_device_type_property_type_image_id unique (image_id),
  constraint uq_device_type_property_type_organization_id_name unique (organization_id,name),
  constraint pk_device_type_property_type primary key (id)
);

create table floor (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  name                          varchar(255),
  local_id                      integer,
  image_id                      varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint uq_floor_image_id unique (image_id),
  constraint pk_floor primary key (id)
);

create table floor_device (
  floor_id                      varchar(36) not null,
  device_id                     varchar(36) not null,
  constraint pk_floor_device primary key (floor_id,device_id)
);

create table floor_floor_tag (
  floor_id                      varchar(36) not null,
  floor_tag_text                varchar(255) not null,
  constraint pk_floor_floor_tag primary key (floor_id,floor_tag_text)
);

create table floor_tag (
  text                          varchar(255) not null,
  organization_id               varchar(36),
  when_created                  datetime(6) not null,
  constraint pk_floor_tag primary key (text)
);

create table history (
  id                            varchar(255) not null,
  entity_id                     varchar(255),
  entity_version                bigint,
  entity_class                  varchar(255),
  data_before                   LONGTEXT,
  data_after                    LONGTEXT,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_history primary key (id)
);

create table html (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  data                          MEDIUMTEXT,
  notes                         varchar(255),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_html primary key (id)
);

create table license (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  max_messages                  integer,
  name                          varchar(64) not null,
  status                        integer,
  is_client                     tinyint(1),
  renewed_time                  datetime(6),
  expires_time                  datetime(6),
  dashboard_module_access       tinyint(1),
  devices_module_access         tinyint(1),
  projects_module_access        tinyint(1),
  workflow_module_access        tinyint(1),
  issues_module_access          tinyint(1),
  collaborator_module_access    tinyint(1),
  teams_module_access           tinyint(1),
  analytics_module_access       tinyint(1),
  members_module_access         tinyint(1),
  messages_module_access        tinyint(1),
  marketplace_module_access     tinyint(1),
  developer_module_access       tinyint(1),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint ck_license_status check ( status in (0,1,2)),
  constraint pk_license primary key (id)
);

create table organization (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  external_id                   varchar(255),
  name                          varchar(255) not null,
  email_domain                  varchar(64),
  workspace_name                varchar(64),
  place_id                      varchar(36),
  enabled                       tinyint(1),
  approved                      tinyint(1),
  self_service_signup           tinyint(1),
  slack_hook                    varchar(511),
  license_id                    varchar(36),
  by_id                         varchar(36),
  approved_by_id                varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint uq_organization_email_domain unique (email_domain),
  constraint uq_organization_workspace_name unique (workspace_name),
  constraint uq_organization_place_id unique (place_id),
  constraint uq_organization_license_id unique (license_id),
  constraint pk_organization primary key (id)
);

create table collaborator_organizations (
  organization_id               varchar(36) not null,
  collaborator_organization_id  varchar(36) not null,
  constraint pk_collaborator_organizations primary key (organization_id,collaborator_organization_id)
);

create table peer (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  name                          varchar(150),
  host_address                  varchar(255),
  host_name                     varchar(255),
  port                          varchar(255),
  service_type                  varchar(255),
  device_id                     varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint uq_peer_device_id unique (device_id),
  constraint pk_peer primary key (id)
);

create table place (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  name                          varchar(255),
  address                       varchar(255),
  city                          varchar(255),
  state                         varchar(255),
  telephone_number              varchar(255),
  zip                           varchar(255),
  place_type_id                 varchar(36),
  lat                           varchar(255),
  lon                           varchar(255),
  cartesian_x                   integer not null,
  cartesian_y                   integer not null,
  floor_id                      varchar(36),
  user_id                       varchar(36),
  notes                         varchar(255),
  verified                      tinyint(1) default 0 not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_place primary key (id)
);

create table place_type (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  name                          varchar(255) not null,
  description                   varchar(255),
  label_class                   varchar(255),
  image_id                      varchar(36),
  visibility                    integer not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint ck_place_type_visibility check ( visibility in (0,1,2,3)),
  constraint uq_place_type_image_id unique (image_id),
  constraint uq_place_type_organization_id_name unique (organization_id,name),
  constraint pk_place_type primary key (id)
);

create table product (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  name                          varchar(255),
  description                   varchar(255),
  notes                         varchar(255),
  verified                      tinyint(1),
  enabled                       tinyint(1),
  by_id                         varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_product primary key (id)
);

create table product_tag (
  product_id                    varchar(36) not null,
  tag_text                      varchar(150) not null,
  constraint pk_product_tag primary key (product_id,tag_text)
);

create table product_release (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  download_url                  varchar(255),
  product_id                    varchar(36),
  name                          varchar(64) not null,
  version_x                     integer not null,
  version_y                     integer not null,
  version_z                     integer not null,
  beta                          tinyint(1) default 0 not null,
  shipped                       tinyint(1) default 0 not null,
  upgrade_id                    varchar(36),
  by_id                         varchar(36),
  html_id                       varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  when_shipped                  datetime(6) not null,
  constraint uq_product_release_upgrade_id unique (upgrade_id),
  constraint uq_product_release_html_id unique (html_id),
  constraint uq_product_release_product_id_version_x_version_y_version_z unique (product_id,version_x,version_y,version_z),
  constraint pk_product_release primary key (id)
);

create table reader_sighting (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  reader_device_id              varchar(36),
  sighted_device_id             varchar(36),
  sighted_user_id               varchar(36),
  rssi                          varchar(255),
  distance                      varchar(255),
  temperature                   varchar(255),
  battery_level                 varchar(255),
  when_seen                     datetime(6),
  sent                          tinyint(1) default 0 not null,
  processed                     tinyint(1) default 0 not null,
  visit_id                      varchar(36),
  place_id                      varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_reader_sighting primary key (id)
);

create table reader_visit_report (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  reader_device_id              varchar(36),
  when_seen                     datetime(6),
  android_count                 integer not null,
  apple_count                   integer not null,
  laptop_counts                 integer not null,
  networking_devices_count      integer not null,
  masked_devices_count          integer not null,
  not_in_ouicount               integer not null,
  others_count                  integer not null,
  devices_count                 integer not null,
  resolved_devices_count        integer not null,
  sent                          tinyint(1) default 0 not null,
  place_id                      varchar(36),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_reader_visit_report primary key (id)
);

create table role (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  name                          varchar(255),
  description                   varchar(255),
  role_image_id                 varchar(36),
  available_publicly            tinyint(1),
  dashboard_module_access       integer,
  devices_module_access         integer,
  projects_module_access        integer,
  workflow_module_access        integer,
  issues_module_access          integer,
  collaborator_module_access    integer,
  teams_module_access           integer,
  analytics_module_access       integer,
  members_module_access         integer,
  messages_module_access        integer,
  marketplace_module_access     integer,
  developer_module_access       integer,
  default_route                 varchar(255),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint ck_role_dashboard_module_access check ( dashboard_module_access in (0,1,2)),
  constraint ck_role_devices_module_access check ( devices_module_access in (0,1,2)),
  constraint ck_role_projects_module_access check ( projects_module_access in (0,1,2)),
  constraint ck_role_workflow_module_access check ( workflow_module_access in (0,1,2)),
  constraint ck_role_issues_module_access check ( issues_module_access in (0,1,2)),
  constraint ck_role_collaborator_module_access check ( collaborator_module_access in (0,1,2)),
  constraint ck_role_teams_module_access check ( teams_module_access in (0,1,2)),
  constraint ck_role_analytics_module_access check ( analytics_module_access in (0,1,2)),
  constraint ck_role_members_module_access check ( members_module_access in (0,1,2)),
  constraint ck_role_messages_module_access check ( messages_module_access in (0,1,2)),
  constraint ck_role_marketplace_module_access check ( marketplace_module_access in (0,1,2)),
  constraint ck_role_developer_module_access check ( developer_module_access in (0,1,2)),
  constraint uq_role_organization_id_name unique (organization_id,name),
  constraint pk_role primary key (id)
);

create table settings (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  email_notification            integer,
  daily_activity_email          integer,
  in_app_notification           integer,
  flow_notification             integer,
  device_notification           integer,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_settings primary key (id)
);

create table sighting (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  reader_device_id              varchar(36),
  sighted_device_id             varchar(36),
  sighted_user_id               varchar(36),
  rssi                          varchar(255),
  k_rssi                        varchar(255),
  distance                      varchar(255),
  k_distance                    varchar(255),
  temperature                   varchar(255),
  battery_level                 varchar(255),
  when_seen                     datetime(6),
  floor_id                      varchar(36),
  visit_id                      varchar(36),
  processed                     tinyint(1) default 0 not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_sighting primary key (id)
);

create table tag (
  text                          varchar(150) not null,
  when_created                  datetime(6) not null,
  constraint pk_tag primary key (text)
);

create table type_image (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  organization_id               varchar(36),
  by_id                         varchar(36),
  url                           varchar(255),
  w                             integer,
  h                             integer,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_type_image primary key (id)
);

create table user (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  email                         varbinary(255),
  strong_password               varchar(512),
  first_name                    varchar(255),
  external_id                   varchar(255),
  last_name                     varchar(255),
  phone_number                  varchar(64),
  bio                           varchar(512),
  totpkey                       varchar(255),
  totpconfigured                tinyint(1),
  country_code                  varchar(6),
  image_id                      varchar(36),
  confirmation_hash             varchar(255),
  confirmed                     tinyint(1),
  reset_token                   varchar(255),
  place_id                      varchar(36),
  pin                           varchar(255),
  organization_id               varchar(36),
  role_id                       varchar(36),
  settings_id                   varchar(36),
  enabled                       tinyint(1),
  is_admin                      tinyint(1),
  is_superadmin                 tinyint(1),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint uq_user_image_id unique (image_id),
  constraint uq_user_place_id unique (place_id),
  constraint uq_user_settings_id unique (settings_id),
  constraint pk_user primary key (id)
);

create table visit (
  id                            varchar(36) not null,
  deleted                       tinyint(1) default 0 not null,
  endpoint_device_id            varchar(36),
  user_device_id                varchar(36),
  user_id                       varchar(36),
  at_id                         varchar(36),
  when_started                  datetime(6),
  when_ended                    datetime(6),
  sent                          tinyint(1) default 0 not null,
  processed                     tinyint(1) default 0 not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_visit primary key (id)
);

create index ix_peer_name on peer (name);
create index ix_tag_text on tag (text);
alter table config add constraint fk_config_device_id foreign key (device_id) references device (id) on delete restrict on update restrict;

alter table config add constraint fk_config_place_id foreign key (place_id) references place (id) on delete restrict on update restrict;

alter table config add constraint fk_config_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_device_organization_id on device (organization_id);
alter table device add constraint fk_device_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_device_by_id on device (by_id);
alter table device add constraint fk_device_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

create index ix_device_owner_id on device (owner_id);
alter table device add constraint fk_device_owner_id foreign key (owner_id) references user (id) on delete restrict on update restrict;

create index ix_device_manufacturer_id on device (manufacturer_id);
alter table device add constraint fk_device_manufacturer_id foreign key (manufacturer_id) references organization (id) on delete restrict on update restrict;

alter table device add constraint fk_device_current_place_id foreign key (current_place_id) references place (id) on delete restrict on update restrict;

create index ix_device_device_type_id on device (device_type_id);
alter table device add constraint fk_device_device_type_id foreign key (device_type_id) references device_type (id) on delete restrict on update restrict;

create index ix_device_software_id on device (software_id);
alter table device add constraint fk_device_software_id foreign key (software_id) references product_release (id) on delete restrict on update restrict;

create index ix_device_place_device on device_place (device_id);
alter table device_place add constraint fk_device_place_device foreign key (device_id) references device (id) on delete restrict on update restrict;

create index ix_device_place_place on device_place (place_id);
alter table device_place add constraint fk_device_place_place foreign key (place_id) references place (id) on delete restrict on update restrict;

create index ix_device_collaborator_organizations_device on device_collaborator_organizations (device_id);
alter table device_collaborator_organizations add constraint fk_device_collaborator_organizations_device foreign key (device_id) references device (id) on delete restrict on update restrict;

create index ix_device_collaborator_organizations_organization on device_collaborator_organizations (collaborator_organization_id);
alter table device_collaborator_organizations add constraint fk_device_collaborator_organizations_organization foreign key (collaborator_organization_id) references organization (id) on delete restrict on update restrict;

create index ix_device_collaborator_users_device on device_collaborator_users (device_id);
alter table device_collaborator_users add constraint fk_device_collaborator_users_device foreign key (device_id) references device (id) on delete restrict on update restrict;

create index ix_device_collaborator_users_user on device_collaborator_users (user_id);
alter table device_collaborator_users add constraint fk_device_collaborator_users_user foreign key (user_id) references user (id) on delete restrict on update restrict;

create index ix_device_type_organization_id on device_type (organization_id);
alter table device_type add constraint fk_device_type_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_device_type_by_id on device_type (by_id);
alter table device_type add constraint fk_device_type_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table device_type add constraint fk_device_type_image_id foreign key (image_id) references type_image (id) on delete restrict on update restrict;

create index ix_device_type_html_id on device_type (html_id);
alter table device_type add constraint fk_device_type_html_id foreign key (html_id) references html (id) on delete restrict on update restrict;

create index ix_device_type_properties_device_type on device_type_properties (device_type_id);
alter table device_type_properties add constraint fk_device_type_properties_device_type foreign key (device_type_id) references device_type (id) on delete restrict on update restrict;

create index ix_device_type_properties_device_type_property on device_type_properties (device_type_property_id);
alter table device_type_properties add constraint fk_device_type_properties_device_type_property foreign key (device_type_property_id) references device_type_property (id) on delete restrict on update restrict;

create index ix_device_type_property_organization_id on device_type_property (organization_id);
alter table device_type_property add constraint fk_device_type_property_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_device_type_property_by_id on device_type_property (by_id);
alter table device_type_property add constraint fk_device_type_property_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table device_type_property add constraint fk_device_type_property_image_id foreign key (image_id) references type_image (id) on delete restrict on update restrict;

create index ix_device_type_property_device_type_property_type_id on device_type_property (device_type_property_type_id);
alter table device_type_property add constraint fk_device_type_property_device_type_property_type_id foreign key (device_type_property_type_id) references device_type_property_type (id) on delete restrict on update restrict;

create index ix_device_type_property_device_type_property_status_id on device_type_property (device_type_property_status_id);
alter table device_type_property add constraint fk_device_type_property_device_type_property_status_id foreign key (device_type_property_status_id) references device_type_property_status (id) on delete restrict on update restrict;

create index ix_device_type_property_status_organization_id on device_type_property_status (organization_id);
alter table device_type_property_status add constraint fk_device_type_property_status_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_device_type_property_status_by_id on device_type_property_status (by_id);
alter table device_type_property_status add constraint fk_device_type_property_status_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table device_type_property_status add constraint fk_device_type_property_status_image_id foreign key (image_id) references type_image (id) on delete restrict on update restrict;

create index ix_device_type_property_type_organization_id on device_type_property_type (organization_id);
alter table device_type_property_type add constraint fk_device_type_property_type_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_device_type_property_type_by_id on device_type_property_type (by_id);
alter table device_type_property_type add constraint fk_device_type_property_type_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table device_type_property_type add constraint fk_device_type_property_type_image_id foreign key (image_id) references type_image (id) on delete restrict on update restrict;

create index ix_floor_organization_id on floor (organization_id);
alter table floor add constraint fk_floor_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_floor_by_id on floor (by_id);
alter table floor add constraint fk_floor_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table floor add constraint fk_floor_image_id foreign key (image_id) references type_image (id) on delete restrict on update restrict;

create index ix_floor_device_floor on floor_device (floor_id);
alter table floor_device add constraint fk_floor_device_floor foreign key (floor_id) references floor (id) on delete restrict on update restrict;

create index ix_floor_device_device on floor_device (device_id);
alter table floor_device add constraint fk_floor_device_device foreign key (device_id) references device (id) on delete restrict on update restrict;

create index ix_floor_floor_tag_floor on floor_floor_tag (floor_id);
alter table floor_floor_tag add constraint fk_floor_floor_tag_floor foreign key (floor_id) references floor (id) on delete restrict on update restrict;

create index ix_floor_floor_tag_floor_tag on floor_floor_tag (floor_tag_text);
alter table floor_floor_tag add constraint fk_floor_floor_tag_floor_tag foreign key (floor_tag_text) references floor_tag (text) on delete restrict on update restrict;

create index ix_floor_tag_organization_id on floor_tag (organization_id);
alter table floor_tag add constraint fk_floor_tag_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_html_organization_id on html (organization_id);
alter table html add constraint fk_html_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_html_by_id on html (by_id);
alter table html add constraint fk_html_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table organization add constraint fk_organization_place_id foreign key (place_id) references place (id) on delete restrict on update restrict;

alter table organization add constraint fk_organization_license_id foreign key (license_id) references license (id) on delete restrict on update restrict;

create index ix_organization_by_id on organization (by_id);
alter table organization add constraint fk_organization_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

create index ix_organization_approved_by_id on organization (approved_by_id);
alter table organization add constraint fk_organization_approved_by_id foreign key (approved_by_id) references user (id) on delete restrict on update restrict;

create index ix_collaborator_organizations_organization_1 on collaborator_organizations (organization_id);
alter table collaborator_organizations add constraint fk_collaborator_organizations_organization_1 foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_collaborator_organizations_organization_2 on collaborator_organizations (collaborator_organization_id);
alter table collaborator_organizations add constraint fk_collaborator_organizations_organization_2 foreign key (collaborator_organization_id) references organization (id) on delete restrict on update restrict;

alter table peer add constraint fk_peer_device_id foreign key (device_id) references device (id) on delete restrict on update restrict;

create index ix_place_organization_id on place (organization_id);
alter table place add constraint fk_place_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_place_by_id on place (by_id);
alter table place add constraint fk_place_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

create index ix_place_place_type_id on place (place_type_id);
alter table place add constraint fk_place_place_type_id foreign key (place_type_id) references place_type (id) on delete restrict on update restrict;

create index ix_place_floor_id on place (floor_id);
alter table place add constraint fk_place_floor_id foreign key (floor_id) references floor (id) on delete restrict on update restrict;

create index ix_place_user_id on place (user_id);
alter table place add constraint fk_place_user_id foreign key (user_id) references user (id) on delete restrict on update restrict;

create index ix_place_type_organization_id on place_type (organization_id);
alter table place_type add constraint fk_place_type_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_place_type_by_id on place_type (by_id);
alter table place_type add constraint fk_place_type_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table place_type add constraint fk_place_type_image_id foreign key (image_id) references type_image (id) on delete restrict on update restrict;

create index ix_product_by_id on product (by_id);
alter table product add constraint fk_product_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

create index ix_product_tag_product on product_tag (product_id);
alter table product_tag add constraint fk_product_tag_product foreign key (product_id) references product (id) on delete restrict on update restrict;

create index ix_product_tag_tag on product_tag (tag_text);
alter table product_tag add constraint fk_product_tag_tag foreign key (tag_text) references tag (text) on delete restrict on update restrict;

create index ix_product_release_product_id on product_release (product_id);
alter table product_release add constraint fk_product_release_product_id foreign key (product_id) references product (id) on delete restrict on update restrict;

alter table product_release add constraint fk_product_release_upgrade_id foreign key (upgrade_id) references product_release (id) on delete restrict on update restrict;

create index ix_product_release_by_id on product_release (by_id);
alter table product_release add constraint fk_product_release_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table product_release add constraint fk_product_release_html_id foreign key (html_id) references html (id) on delete restrict on update restrict;

create index ix_reader_sighting_reader_device_id on reader_sighting (reader_device_id);
alter table reader_sighting add constraint fk_reader_sighting_reader_device_id foreign key (reader_device_id) references device (id) on delete restrict on update restrict;

create index ix_reader_sighting_sighted_device_id on reader_sighting (sighted_device_id);
alter table reader_sighting add constraint fk_reader_sighting_sighted_device_id foreign key (sighted_device_id) references device (id) on delete restrict on update restrict;

create index ix_reader_sighting_sighted_user_id on reader_sighting (sighted_user_id);
alter table reader_sighting add constraint fk_reader_sighting_sighted_user_id foreign key (sighted_user_id) references user (id) on delete restrict on update restrict;

create index ix_reader_sighting_visit_id on reader_sighting (visit_id);
alter table reader_sighting add constraint fk_reader_sighting_visit_id foreign key (visit_id) references visit (id) on delete restrict on update restrict;

create index ix_reader_sighting_place_id on reader_sighting (place_id);
alter table reader_sighting add constraint fk_reader_sighting_place_id foreign key (place_id) references place (id) on delete restrict on update restrict;

create index ix_reader_visit_report_organization_id on reader_visit_report (organization_id);
alter table reader_visit_report add constraint fk_reader_visit_report_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_reader_visit_report_by_id on reader_visit_report (by_id);
alter table reader_visit_report add constraint fk_reader_visit_report_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

create index ix_reader_visit_report_reader_device_id on reader_visit_report (reader_device_id);
alter table reader_visit_report add constraint fk_reader_visit_report_reader_device_id foreign key (reader_device_id) references device (id) on delete restrict on update restrict;

create index ix_reader_visit_report_place_id on reader_visit_report (place_id);
alter table reader_visit_report add constraint fk_reader_visit_report_place_id foreign key (place_id) references place (id) on delete restrict on update restrict;

create index ix_role_organization_id on role (organization_id);
alter table role add constraint fk_role_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_role_by_id on role (by_id);
alter table role add constraint fk_role_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

create index ix_role_role_image_id on role (role_image_id);
alter table role add constraint fk_role_role_image_id foreign key (role_image_id) references type_image (id) on delete restrict on update restrict;

create index ix_settings_organization_id on settings (organization_id);
alter table settings add constraint fk_settings_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_settings_by_id on settings (by_id);
alter table settings add constraint fk_settings_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

create index ix_sighting_organization_id on sighting (organization_id);
alter table sighting add constraint fk_sighting_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_sighting_by_id on sighting (by_id);
alter table sighting add constraint fk_sighting_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

create index ix_sighting_reader_device_id on sighting (reader_device_id);
alter table sighting add constraint fk_sighting_reader_device_id foreign key (reader_device_id) references device (id) on delete restrict on update restrict;

create index ix_sighting_sighted_device_id on sighting (sighted_device_id);
alter table sighting add constraint fk_sighting_sighted_device_id foreign key (sighted_device_id) references device (id) on delete restrict on update restrict;

create index ix_sighting_sighted_user_id on sighting (sighted_user_id);
alter table sighting add constraint fk_sighting_sighted_user_id foreign key (sighted_user_id) references user (id) on delete restrict on update restrict;

create index ix_sighting_floor_id on sighting (floor_id);
alter table sighting add constraint fk_sighting_floor_id foreign key (floor_id) references floor (id) on delete restrict on update restrict;

create index ix_sighting_visit_id on sighting (visit_id);
alter table sighting add constraint fk_sighting_visit_id foreign key (visit_id) references visit (id) on delete restrict on update restrict;

create index ix_type_image_organization_id on type_image (organization_id);
alter table type_image add constraint fk_type_image_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_type_image_by_id on type_image (by_id);
alter table type_image add constraint fk_type_image_by_id foreign key (by_id) references user (id) on delete restrict on update restrict;

alter table user add constraint fk_user_image_id foreign key (image_id) references type_image (id) on delete restrict on update restrict;

alter table user add constraint fk_user_place_id foreign key (place_id) references place (id) on delete restrict on update restrict;

create index ix_user_organization_id on user (organization_id);
alter table user add constraint fk_user_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;

create index ix_user_role_id on user (role_id);
alter table user add constraint fk_user_role_id foreign key (role_id) references role (id) on delete restrict on update restrict;

alter table user add constraint fk_user_settings_id foreign key (settings_id) references settings (id) on delete restrict on update restrict;

create index ix_visit_endpoint_device_id on visit (endpoint_device_id);
alter table visit add constraint fk_visit_endpoint_device_id foreign key (endpoint_device_id) references device (id) on delete restrict on update restrict;

create index ix_visit_user_device_id on visit (user_device_id);
alter table visit add constraint fk_visit_user_device_id foreign key (user_device_id) references device (id) on delete restrict on update restrict;

create index ix_visit_user_id on visit (user_id);
alter table visit add constraint fk_visit_user_id foreign key (user_id) references user (id) on delete restrict on update restrict;

create index ix_visit_at_id on visit (at_id);
alter table visit add constraint fk_visit_at_id foreign key (at_id) references place (id) on delete restrict on update restrict;


# --- !Downs

alter table config drop foreign key fk_config_device_id;

alter table config drop foreign key fk_config_place_id;

alter table config drop foreign key fk_config_organization_id;

alter table device drop foreign key fk_device_organization_id;
drop index ix_device_organization_id on device;

alter table device drop foreign key fk_device_by_id;
drop index ix_device_by_id on device;

alter table device drop foreign key fk_device_owner_id;
drop index ix_device_owner_id on device;

alter table device drop foreign key fk_device_manufacturer_id;
drop index ix_device_manufacturer_id on device;

alter table device drop foreign key fk_device_current_place_id;

alter table device drop foreign key fk_device_device_type_id;
drop index ix_device_device_type_id on device;

alter table device drop foreign key fk_device_software_id;
drop index ix_device_software_id on device;

alter table device_place drop foreign key fk_device_place_device;
drop index ix_device_place_device on device_place;

alter table device_place drop foreign key fk_device_place_place;
drop index ix_device_place_place on device_place;

alter table device_collaborator_organizations drop foreign key fk_device_collaborator_organizations_device;
drop index ix_device_collaborator_organizations_device on device_collaborator_organizations;

alter table device_collaborator_organizations drop foreign key fk_device_collaborator_organizations_organization;
drop index ix_device_collaborator_organizations_organization on device_collaborator_organizations;

alter table device_collaborator_users drop foreign key fk_device_collaborator_users_device;
drop index ix_device_collaborator_users_device on device_collaborator_users;

alter table device_collaborator_users drop foreign key fk_device_collaborator_users_user;
drop index ix_device_collaborator_users_user on device_collaborator_users;

alter table device_type drop foreign key fk_device_type_organization_id;
drop index ix_device_type_organization_id on device_type;

alter table device_type drop foreign key fk_device_type_by_id;
drop index ix_device_type_by_id on device_type;

alter table device_type drop foreign key fk_device_type_image_id;

alter table device_type drop foreign key fk_device_type_html_id;
drop index ix_device_type_html_id on device_type;

alter table device_type_properties drop foreign key fk_device_type_properties_device_type;
drop index ix_device_type_properties_device_type on device_type_properties;

alter table device_type_properties drop foreign key fk_device_type_properties_device_type_property;
drop index ix_device_type_properties_device_type_property on device_type_properties;

alter table device_type_property drop foreign key fk_device_type_property_organization_id;
drop index ix_device_type_property_organization_id on device_type_property;

alter table device_type_property drop foreign key fk_device_type_property_by_id;
drop index ix_device_type_property_by_id on device_type_property;

alter table device_type_property drop foreign key fk_device_type_property_image_id;

alter table device_type_property drop foreign key fk_device_type_property_device_type_property_type_id;
drop index ix_device_type_property_device_type_property_type_id on device_type_property;

alter table device_type_property drop foreign key fk_device_type_property_device_type_property_status_id;
drop index ix_device_type_property_device_type_property_status_id on device_type_property;

alter table device_type_property_status drop foreign key fk_device_type_property_status_organization_id;
drop index ix_device_type_property_status_organization_id on device_type_property_status;

alter table device_type_property_status drop foreign key fk_device_type_property_status_by_id;
drop index ix_device_type_property_status_by_id on device_type_property_status;

alter table device_type_property_status drop foreign key fk_device_type_property_status_image_id;

alter table device_type_property_type drop foreign key fk_device_type_property_type_organization_id;
drop index ix_device_type_property_type_organization_id on device_type_property_type;

alter table device_type_property_type drop foreign key fk_device_type_property_type_by_id;
drop index ix_device_type_property_type_by_id on device_type_property_type;

alter table device_type_property_type drop foreign key fk_device_type_property_type_image_id;

alter table floor drop foreign key fk_floor_organization_id;
drop index ix_floor_organization_id on floor;

alter table floor drop foreign key fk_floor_by_id;
drop index ix_floor_by_id on floor;

alter table floor drop foreign key fk_floor_image_id;

alter table floor_device drop foreign key fk_floor_device_floor;
drop index ix_floor_device_floor on floor_device;

alter table floor_device drop foreign key fk_floor_device_device;
drop index ix_floor_device_device on floor_device;

alter table floor_floor_tag drop foreign key fk_floor_floor_tag_floor;
drop index ix_floor_floor_tag_floor on floor_floor_tag;

alter table floor_floor_tag drop foreign key fk_floor_floor_tag_floor_tag;
drop index ix_floor_floor_tag_floor_tag on floor_floor_tag;

alter table floor_tag drop foreign key fk_floor_tag_organization_id;
drop index ix_floor_tag_organization_id on floor_tag;

alter table html drop foreign key fk_html_organization_id;
drop index ix_html_organization_id on html;

alter table html drop foreign key fk_html_by_id;
drop index ix_html_by_id on html;

alter table organization drop foreign key fk_organization_place_id;

alter table organization drop foreign key fk_organization_license_id;

alter table organization drop foreign key fk_organization_by_id;
drop index ix_organization_by_id on organization;

alter table organization drop foreign key fk_organization_approved_by_id;
drop index ix_organization_approved_by_id on organization;

alter table collaborator_organizations drop foreign key fk_collaborator_organizations_organization_1;
drop index ix_collaborator_organizations_organization_1 on collaborator_organizations;

alter table collaborator_organizations drop foreign key fk_collaborator_organizations_organization_2;
drop index ix_collaborator_organizations_organization_2 on collaborator_organizations;

alter table peer drop foreign key fk_peer_device_id;

alter table place drop foreign key fk_place_organization_id;
drop index ix_place_organization_id on place;

alter table place drop foreign key fk_place_by_id;
drop index ix_place_by_id on place;

alter table place drop foreign key fk_place_place_type_id;
drop index ix_place_place_type_id on place;

alter table place drop foreign key fk_place_floor_id;
drop index ix_place_floor_id on place;

alter table place drop foreign key fk_place_user_id;
drop index ix_place_user_id on place;

alter table place_type drop foreign key fk_place_type_organization_id;
drop index ix_place_type_organization_id on place_type;

alter table place_type drop foreign key fk_place_type_by_id;
drop index ix_place_type_by_id on place_type;

alter table place_type drop foreign key fk_place_type_image_id;

alter table product drop foreign key fk_product_by_id;
drop index ix_product_by_id on product;

alter table product_tag drop foreign key fk_product_tag_product;
drop index ix_product_tag_product on product_tag;

alter table product_tag drop foreign key fk_product_tag_tag;
drop index ix_product_tag_tag on product_tag;

alter table product_release drop foreign key fk_product_release_product_id;
drop index ix_product_release_product_id on product_release;

alter table product_release drop foreign key fk_product_release_upgrade_id;

alter table product_release drop foreign key fk_product_release_by_id;
drop index ix_product_release_by_id on product_release;

alter table product_release drop foreign key fk_product_release_html_id;

alter table reader_sighting drop foreign key fk_reader_sighting_reader_device_id;
drop index ix_reader_sighting_reader_device_id on reader_sighting;

alter table reader_sighting drop foreign key fk_reader_sighting_sighted_device_id;
drop index ix_reader_sighting_sighted_device_id on reader_sighting;

alter table reader_sighting drop foreign key fk_reader_sighting_sighted_user_id;
drop index ix_reader_sighting_sighted_user_id on reader_sighting;

alter table reader_sighting drop foreign key fk_reader_sighting_visit_id;
drop index ix_reader_sighting_visit_id on reader_sighting;

alter table reader_sighting drop foreign key fk_reader_sighting_place_id;
drop index ix_reader_sighting_place_id on reader_sighting;

alter table reader_visit_report drop foreign key fk_reader_visit_report_organization_id;
drop index ix_reader_visit_report_organization_id on reader_visit_report;

alter table reader_visit_report drop foreign key fk_reader_visit_report_by_id;
drop index ix_reader_visit_report_by_id on reader_visit_report;

alter table reader_visit_report drop foreign key fk_reader_visit_report_reader_device_id;
drop index ix_reader_visit_report_reader_device_id on reader_visit_report;

alter table reader_visit_report drop foreign key fk_reader_visit_report_place_id;
drop index ix_reader_visit_report_place_id on reader_visit_report;

alter table role drop foreign key fk_role_organization_id;
drop index ix_role_organization_id on role;

alter table role drop foreign key fk_role_by_id;
drop index ix_role_by_id on role;

alter table role drop foreign key fk_role_role_image_id;
drop index ix_role_role_image_id on role;

alter table settings drop foreign key fk_settings_organization_id;
drop index ix_settings_organization_id on settings;

alter table settings drop foreign key fk_settings_by_id;
drop index ix_settings_by_id on settings;

alter table sighting drop foreign key fk_sighting_organization_id;
drop index ix_sighting_organization_id on sighting;

alter table sighting drop foreign key fk_sighting_by_id;
drop index ix_sighting_by_id on sighting;

alter table sighting drop foreign key fk_sighting_reader_device_id;
drop index ix_sighting_reader_device_id on sighting;

alter table sighting drop foreign key fk_sighting_sighted_device_id;
drop index ix_sighting_sighted_device_id on sighting;

alter table sighting drop foreign key fk_sighting_sighted_user_id;
drop index ix_sighting_sighted_user_id on sighting;

alter table sighting drop foreign key fk_sighting_floor_id;
drop index ix_sighting_floor_id on sighting;

alter table sighting drop foreign key fk_sighting_visit_id;
drop index ix_sighting_visit_id on sighting;

alter table type_image drop foreign key fk_type_image_organization_id;
drop index ix_type_image_organization_id on type_image;

alter table type_image drop foreign key fk_type_image_by_id;
drop index ix_type_image_by_id on type_image;

alter table user drop foreign key fk_user_image_id;

alter table user drop foreign key fk_user_place_id;

alter table user drop foreign key fk_user_organization_id;
drop index ix_user_organization_id on user;

alter table user drop foreign key fk_user_role_id;
drop index ix_user_role_id on user;

alter table user drop foreign key fk_user_settings_id;

alter table visit drop foreign key fk_visit_endpoint_device_id;
drop index ix_visit_endpoint_device_id on visit;

alter table visit drop foreign key fk_visit_user_device_id;
drop index ix_visit_user_device_id on visit;

alter table visit drop foreign key fk_visit_user_id;
drop index ix_visit_user_id on visit;

alter table visit drop foreign key fk_visit_at_id;
drop index ix_visit_at_id on visit;

drop table if exists config;

drop table if exists data_migration;

drop table if exists device;

drop table if exists device_place;

drop table if exists device_collaborator_organizations;

drop table if exists device_collaborator_users;

drop table if exists device_type;

drop table if exists device_type_properties;

drop table if exists device_type_property;

drop table if exists device_type_property_status;

drop table if exists device_type_property_type;

drop table if exists floor;

drop table if exists floor_device;

drop table if exists floor_floor_tag;

drop table if exists floor_tag;

drop table if exists history;

drop table if exists html;

drop table if exists license;

drop table if exists organization;

drop table if exists collaborator_organizations;

drop table if exists peer;

drop table if exists place;

drop table if exists place_type;

drop table if exists product;

drop table if exists product_tag;

drop table if exists product_release;

drop table if exists reader_sighting;

drop table if exists reader_visit_report;

drop table if exists role;

drop table if exists settings;

drop table if exists sighting;

drop table if exists tag;

drop table if exists type_image;

drop table if exists user;

drop table if exists visit;

drop index ix_peer_name on peer;
drop index ix_tag_text on tag;
