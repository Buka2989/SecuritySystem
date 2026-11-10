-- Run once against the existing Java PostgreSQL database.
CREATE TABLE IF NOT EXISTS public.notifications (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id uuid NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title varchar(150) NOT NULL,
    message varchar(1000) NOT NULL,
    type varchar(40) NOT NULL,
    is_read boolean NOT NULL DEFAULT false,
    created_at timestamp without time zone NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_created
    ON public.notifications (recipient_user_id, created_at DESC);
