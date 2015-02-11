import ctypes

__all__ = ('ALL_CONSTANTS', 'CHAR_MAX', 'HAS_LANGINFO', 'LC_ALL', 'LC_COLLATE', 'LC_CTYPE', 'LC_MONETARY', 'LC_NUMERIC', 'LC_TIME')

ALL_CONSTANTS = ('LC_CTYPE', 'LC_TIME', 'LC_COLLATE', 'LC_MONETARY', 'LC_NUMERIC', 'LC_ALL', 'CHAR_MAX')
CHAR_MAX = 127
HAS_LANGINFO = 0
LC_ALL = 0
LC_COLLATE = 1
LC_CTYPE = 2
LC_MONETARY = 3
LC_NUMERIC = 4
LC_TIME = 5