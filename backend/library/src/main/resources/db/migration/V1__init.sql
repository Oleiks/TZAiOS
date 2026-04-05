create table app_users (
  id uuid primary key,
  email varchar(320) not null unique,
  password_hash varchar(255) not null,
  display_name varchar(120) not null,
  created_at timestamp not null
);

create table refresh_tokens (
  id uuid primary key,
  user_id uuid not null references app_users(id) on delete cascade,
  token_hash varchar(128) not null unique,
  created_at timestamp not null,
  expires_at timestamp not null,
  revoked_at timestamp null
);

create index idx_refresh_tokens_user_id on refresh_tokens(user_id);

create table wishlist_items (
  id uuid primary key,
  user_id uuid not null references app_users(id) on delete cascade,
  book_key varchar(255) not null,
  title text not null,
  author_name text not null,
  cover_url text null,
  book_json text not null,
  added_at timestamp not null
);

create index idx_wishlist_items_user_id on wishlist_items(user_id);
create unique index uq_wishlist_items_user_book on wishlist_items(user_id, book_key);

create table api_cache_entries (
  id uuid primary key,
  cache_key varchar(512) not null unique,
  payload_json text not null,
  fetched_at timestamp not null,
  expires_at timestamp not null,
  stale_until timestamp not null
);
