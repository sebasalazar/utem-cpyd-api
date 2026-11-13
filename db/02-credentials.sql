BEGIN TRANSACTION;

TRUNCATE TABLE credentials RESTART IDENTITY CASCADE;
INSERT INTO credentials (email, api_key) VALUES ('santileoa@utem.cl','21.078.915-4');
INSERT INTO credentials (email, api_key) VALUES ('japablaza@utem.cl','21.055.387-8');
INSERT INTO credentials (email, api_key) VALUES ('carayape@utem.cl','21.342.802-0');
INSERT INTO credentials (email, api_key) VALUES ('mcabello@utem.cl','20.648.265-6');
INSERT INTO credentials (email, api_key) VALUES ('fcanalesh@utem.cl','20.470.394-9');
INSERT INTO credentials (email, api_key) VALUES ('fchaparro@utem.cl','20.561.471-0');
INSERT INTO credentials (email, api_key) VALUES ('mdios@utem.cl','20.885.694-4');
INSERT INTO credentials (email, api_key) VALUES ('gdurans@utem.cl','21.498.480-6');
INSERT INTO credentials (email, api_key) VALUES ('mfaundezb@utem.cl','20.417.760-0');
INSERT INTO credentials (email, api_key) VALUES ('ggabrielli@utem.cl','21.112.589-6');
INSERT INTO credentials (email, api_key) VALUES ('jgalvez@utem.cl','21.117.725-K');
INSERT INTO credentials (email, api_key) VALUES ('chuentecuram@utem.cl','21.226.507-1');
INSERT INTO credentials (email, api_key) VALUES ('mmachuca@utem.cl','20.123.051-9');
INSERT INTO credentials (email, api_key) VALUES ('tmartinez@utem.cl','21.226.209-9');
INSERT INTO credentials (email, api_key) VALUES ('lmoral@utem.cl','21.140.440-K');
INSERT INTO credentials (email, api_key) VALUES ('rmoyano@utem.cl','21.474.429-5');
INSERT INTO credentials (email, api_key) VALUES ('andres.munozg@utem.cl','19.164.393-3');
INSERT INTO credentials (email, api_key) VALUES ('joliveros@utem.cl','21.569.379-1');
INSERT INTO credentials (email, api_key) VALUES ('mparrac@utem.cl','21.228.730-K');
INSERT INTO credentials (email, api_key) VALUES ('bquilodran@utem.cl','21.515.957-4');
INSERT INTO credentials (email, api_key) VALUES ('sramirez@utem.cl','20.852.869-6');
INSERT INTO credentials (email, api_key) VALUES ('mrodriguezli@utem.cl','21.495.460-5');
INSERT INTO credentials (email, api_key) VALUES ('itorrealba@utem.cl','21.230.131-0');

INSERT INTO credentials (email, api_key) VALUES ('ssalazar@utem.cl','Luna.25171');
COMMIT;