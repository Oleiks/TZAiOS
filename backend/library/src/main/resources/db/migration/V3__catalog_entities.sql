create table catalog_authors (
  id uuid primary key,
  catalog_key varchar(255) not null unique,
  name varchar(255) not null,
  bio text null,
  personal_name text null
);

create table catalog_author_photos (
  author_id uuid not null references catalog_authors(id) on delete cascade,
  photo_id bigint not null,
  primary key (author_id, photo_id)
);

create index idx_catalog_author_photos_author_id on catalog_author_photos(author_id);

create table catalog_books (
  id uuid primary key,
  catalog_key varchar(255) not null unique,
  title varchar(512) not null,
  primary_author_name text null,
  description text null,
  first_sentence text null,
  notes text null,
  excerpt text null,
  cover_url text null,
  first_publish_year integer null,
  rating double precision null,
  edition_count integer not null
);

create table catalog_book_authors (
  book_id uuid not null references catalog_books(id) on delete cascade,
  author_id uuid not null references catalog_authors(id) on delete cascade,
  primary key (book_id, author_id)
);

create index idx_catalog_book_authors_author_id on catalog_book_authors(author_id);

create table catalog_book_subjects (
  book_id uuid not null references catalog_books(id) on delete cascade,
  subject varchar(255) not null,
  primary key (book_id, subject)
);

create index idx_catalog_book_subjects_book_id on catalog_book_subjects(book_id);

create table catalog_book_work_keys (
  book_id uuid not null references catalog_books(id) on delete cascade,
  work_key varchar(255) not null,
  primary key (book_id, work_key)
);

create index idx_catalog_book_work_keys_book_id on catalog_book_work_keys(book_id);
