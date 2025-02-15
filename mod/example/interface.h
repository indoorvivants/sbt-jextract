typedef enum { MULTIPLY = 1, ADD = 2 } myscalalib_operation;

typedef struct {
  myscalalib_operation op;
  char *label;
} myscalalib_config;

extern float myscalalib_run(myscalalib_config *config, float left, float right);
