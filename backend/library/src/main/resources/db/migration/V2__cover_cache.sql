create table cover_cache_entries (
  id uuid primary key,
  cache_key varchar(512) not null unique,
  content_type varchar(100) not null,
  image_bytes bytea not null,
  fetched_at timestamp with time zone not null
);
