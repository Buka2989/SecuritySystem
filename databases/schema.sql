-- Schema from supplied PostgreSQL backup; no data or ownership statements.
CREATE TABLE public.access_logs (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    qr_code_id uuid NOT NULL,
    security_user_id uuid NOT NULL,
    action character varying(20) NOT NULL,
    result character varying(20) NOT NULL,
    reason character varying(50) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    CONSTRAINT access_logs_action_check CHECK (((action)::text = 'ENTRY'::text)),
    CONSTRAINT access_logs_reason_check CHECK (((reason)::text = ANY ((ARRAY['VALID_QR'::character varying, 'QR_EXPIRED'::character varying, 'QR_ALREADY_USED'::character varying, 'QR_REVOKED'::character varying, 'INVALID_QR'::character varying, 'NOT_VERIFIED'::character varying])::text[]))),
    CONSTRAINT access_logs_result_check CHECK (((result)::text = ANY ((ARRAY['ALLOWED'::character varying, 'DENIED'::character varying])::text[])))
);

CREATE TABLE public.guardian_student (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    student_id uuid NOT NULL,
    guardian_id uuid NOT NULL,
    relationship_type character varying(30) NOT NULL,
    verification_status character varying(20) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    CONSTRAINT guardian_student_relationship_type_check CHECK (((relationship_type)::text = ANY ((ARRAY['FATHER'::character varying, 'MOTHER'::character varying, 'GRANDFATHER'::character varying, 'GRANDMOTHER'::character varying, 'GUARDIAN'::character varying])::text[]))),
    CONSTRAINT guardian_student_verification_status_check CHECK (((verification_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);

CREATE TABLE public.qr_codes (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    guardian_student_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    status character varying(20) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    used_at timestamp without time zone,
    CONSTRAINT qr_codes_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'USED'::character varying, 'EXPIRED'::character varying, 'REVOKED'::character varying])::text[])))
);

CREATE TABLE public.students (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    full_name character varying(150) NOT NULL,
    class_name character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    CONSTRAINT students_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    full_name character varying(150) NOT NULL,
    password_hash character varying(255) NOT NULL,
    role character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    username character varying(50) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['PARENT'::character varying, 'TRUSTED_PERSON'::character varying, 'SECURITY'::character varying, 'ADMIN'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'BLOCKED'::character varying])::text[])))
);

CREATE TABLE public.verification_requests (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    guardian_student_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    comment text,
    created_at timestamp without time zone NOT NULL,
    reviewed_at timestamp without time zone,
    CONSTRAINT verification_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);

ALTER TABLE ONLY public.access_logs
    ADD CONSTRAINT access_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.guardian_student
    ADD CONSTRAINT guardian_student_guardian_id_student_id_key UNIQUE (guardian_id, student_id);

ALTER TABLE ONLY public.guardian_student
    ADD CONSTRAINT guardian_student_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.qr_codes
    ADD CONSTRAINT qr_codes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.qr_codes
    ADD CONSTRAINT qr_codes_token_key UNIQUE (token);

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_unique UNIQUE (username);

ALTER TABLE ONLY public.verification_requests
    ADD CONSTRAINT verification_requests_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.access_logs
    ADD CONSTRAINT access_logs_qr_code_id_fkey FOREIGN KEY (qr_code_id) REFERENCES public.qr_codes(id);

ALTER TABLE ONLY public.access_logs
    ADD CONSTRAINT access_logs_security_user_id_fkey FOREIGN KEY (security_user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.guardian_student
    ADD CONSTRAINT guardian_student_guardian_id_fkey FOREIGN KEY (guardian_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.guardian_student
    ADD CONSTRAINT guardian_student_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.students(id);

ALTER TABLE ONLY public.qr_codes
    ADD CONSTRAINT qr_codes_guardian_student_id_fkey FOREIGN KEY (guardian_student_id) REFERENCES public.guardian_student(id);

ALTER TABLE ONLY public.verification_requests
    ADD CONSTRAINT verification_requests_guardian_student_id_fkey FOREIGN KEY (guardian_student_id) REFERENCES public.guardian_student(id);
