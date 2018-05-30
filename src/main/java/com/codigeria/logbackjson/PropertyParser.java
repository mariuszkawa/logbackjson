package com.codigeria.logbackjson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class PropertyParser
{
    public static void main(String[] args)
    {
        try {
            Map<String, Object> properties = new PropertyParser().parse("integer=3 no_property string=xyz qwerty= longString=\"Hello, World!\"azerty=\"\"qwertz=");
            System.out.println(Arrays.toString(properties.entrySet().toArray()));
        } catch (IllegalParserStateException exception) {
            exception.printStackTrace();
        }
    }

    private Map<String, Object> parse(String input) throws IllegalParserStateException
    {
        ParserContext parserContext = new DefaultParserContext();
        ParserState parserState = ParserStates.INITIAL;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            parserState = parserState.next(c, parserContext);
            if (parserState.isIllegal()) {
                throw new IllegalParserStateException("Illegal parser state");
            }
        }
        parserContext.buildProperty();
        return parserContext.getProperties();
    }


    class IllegalParserStateException extends Exception
    {
        IllegalParserStateException(String message)
        {
            super(message);
        }
    }

    private interface ParserContext
    {
        Map<String,Object> getProperties();

        void appendIdentifierLetter(char c);

        void appendValueLetter(char c);

        void buildProperty();

        void reset();
    }

    private final class DefaultParserContext implements ParserContext
    {
        private final Map<String, Object> properties = new HashMap<>();

        private StringBuilder identifierBuilder = new StringBuilder();

        private StringBuilder valueBuilder = new StringBuilder();

        @Override
        public Map<String, Object> getProperties()
        {
            return properties;
        }

        @Override
        public void appendIdentifierLetter(char c)
        {
            identifierBuilder.append(c);
        }

        @Override
        public void appendValueLetter(char c)
        {
            valueBuilder.append(c);
        }

        @Override
        public void buildProperty()
        {
            String key = identifierBuilder.toString();
            String value = valueBuilder.toString();
            if (!key.isEmpty()) {
                properties.put(key, value);
            }
        }

        @Override
        public void reset()
        {
            identifierBuilder = new StringBuilder();
            valueBuilder = new StringBuilder();
        }
    }

    private interface ParserState
    {
        ParserState next(char c, ParserContext parserContext);

        boolean isIllegal();
    }

    private enum ParserStates implements ParserState
    {
        INITIAL {
            @Override
            public ParserState next(char c, ParserContext parserContext)
            {
                boolean isUpperCaseLetter = (c >= 'A') && (c <= 'Z');
                boolean isLowerCaseLetter = (c >= 'a') && (c <= 'z');
                boolean isUnderscore = (c == '_');
                if (isUpperCaseLetter || isLowerCaseLetter || isUnderscore) {
                    parserContext.appendIdentifierLetter(c);
                    return IDENTIFIER_LETTER;
                }
                return INITIAL;
            }
        },
        IDENTIFIER_LETTER {
            @Override
            public ParserState next(char c, ParserContext parserContext)
            {
                boolean isUpperCaseLetter = (c >= 'A') && (c <= 'Z');
                boolean isLowerCaseLetter = (c >= 'a') && (c <= 'z');
                boolean isDigit = (c >= '0') && (c <= '9');
                boolean isUnderscore = (c == '_');
                if (isUpperCaseLetter || isLowerCaseLetter || isDigit || isUnderscore) {
                    parserContext.appendIdentifierLetter(c);
                    return IDENTIFIER_LETTER;
                } else if (c == '=') {
                    return EQUALITY_SIGN;
                } else {
                    parserContext.reset();
                    return INITIAL;
                }
            }
        },
        EQUALITY_SIGN {
            @Override
            public ParserState next(char c, ParserContext parserContext)
            {
                if (c == ' ') {
                    parserContext.buildProperty();
                    parserContext.reset();
                    return INITIAL;
                } else if (c == '"') {
                    return LONG_VALUE_LETTER;
                } else {
                    parserContext.appendValueLetter(c);
                    return VALUE_LETTER;
                }
            }
        },
        VALUE_LETTER {
            @Override
            public ParserState next(char c, ParserContext parserContext)
            {
                if (c == ' ') {
                    parserContext.buildProperty();
                    parserContext.reset();
                    return INITIAL;
                } else {
                    parserContext.appendValueLetter(c);
                    return VALUE_LETTER;
                }
            }
        },
        LONG_VALUE_LETTER {
            @Override
            public ParserState next(char c, ParserContext parserContext)
            {
                if (c == '"') {
                    parserContext.buildProperty();
                    parserContext.reset();
                    return INITIAL;
                } else {
                    parserContext.appendValueLetter(c);
                    return LONG_VALUE_LETTER;
                }
            }
        },
        ERROR(true) {
            @Override
            public ParserState next(char c, ParserContext parserContext)
            {
                return ERROR;
            }
        };

        private final boolean illegal;

        ParserStates()
        {
            this(false);
        }

        ParserStates(boolean illegal)
        {
            this.illegal = illegal;
        }

        @Override
        public boolean isIllegal()
        {
            return illegal;
        }
    }
}
