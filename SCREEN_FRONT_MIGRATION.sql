-- Einmal im Supabase SQL Editor ausführen.
alter table public.mpsq_screens
  add column if not exists front text not null default 'NORTH'
  check (front in ('NORTH', 'SOUTH', 'EAST', 'WEST', 'UP', 'DOWN'));
