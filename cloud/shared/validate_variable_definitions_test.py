import unittest

from validate_variable_definitions import ValidateVariableDefinitions

class TestValidateVariableDefinitions(unittest.TestCase):
    def test_validate_all_variable_definitions(self):
        validator = ValidateVariableDefinitions()
        validator.load_repo_variable_definitions_files()
        errors = validator.get_validation_errors()

        self.assertEqual(errors, {})

    def test_get_validation_errors_float_no_errors(self):
        enum = {
            "FOO": {
                "required": True,
                "secret": False,
                "type": "float"
            }
        }

        errors = ValidateVariableDefinitions(enum).get_validation_errors()

        self.assertEqual(errors, {})

    def test_get_validation_errors_integer_no_errors(self):
        enum = {
            "FOO": {
                "required": True,
                "secret": False,
                "type": "integer"
            }
        }

        errors = ValidateVariableDefinitions(enum).get_validation_errors()

        self.assertEqual(errors, {})

    def test_get_validation_errors_string_no_errors(self):
        enum = {
            "FOO": {
                "required": True,
                "secret": False,
                "type": "string"
            }
        }

        errors = ValidateVariableDefinitions(enum).get_validation_errors()

        self.assertEqual(errors, {})

    def test_get_validation_errors_string_missing_secret(self):
        enum = {
            "FOO": {
                "required": True,
                "type": "string"
            }
        }

        errors = ValidateVariableDefinitions(enum).get_validation_errors()

        self.assertEqual(errors, { "FOO": ["Missing 'secret' field."] })

    def test_get_validation_errors_string_missing_type(self):
        enum = {
            "FOO": {
                "required": True,
                "secret": False
            }
        }

        errors = ValidateVariableDefinitions(enum).get_validation_errors()

        self.assertEqual(errors, { "FOO": ["Missing 'type' field."] })

    def test_get_validation_errors_string_missing_required(self):
        enum = {
            "FOO": {
                "secret": False,
                "type": "string"
            }
        }

        errors = ValidateVariableDefinitions(enum).get_validation_errors()

        self.assertEqual(errors, { "FOO": ["Missing 'required' field."] })

    def test_get_validation_errors_enum_no_errors(self):
        enum = {
            "CIVIFORM_CLOUD_PROVIDER": {
                "required": True,
                "secret": False,
                "type": "enum",
                "values": ["gcp", "azure", "aws"]
            }
        }

        errors = ValidateVariableDefinitions(enum).get_validation_errors()

        self.assertEqual(errors, {})

    def test_get_validation_errors_enum_missing_values(self):
        enum = {
            "CIVIFORM_CLOUD_PROVIDER": {
                "required": True,
                "secret": False,
                "type": "enum"
            }
        }

        errors = ValidateVariableDefinitions(enum).get_validation_errors()

        expected_errors = {
            "CIVIFORM_CLOUD_PROVIDER": ["Missing 'values' field for enum."]
        }

        self.assertEqual(errors, expected_errors)

if __name__ == '__main__':
    unittest.main()
