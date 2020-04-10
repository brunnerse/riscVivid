/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * (C)opyright 2013-2016 The riscVivid project, University of Augsburg, Germany
 * https://github.com/unia-sik/riscVivid
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, see <LICENSE>. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package riscVivid.asm.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import riscVivid.asm.fsm.CharacterList;
import riscVivid.asm.fsm.FunctionTransition;
import riscVivid.asm.fsm.InverseCharacterList;
import riscVivid.asm.fsm.Procedure;
import riscVivid.asm.fsm.State;
import riscVivid.asm.fsm.Transition;
import riscVivid.asm.instruction.Instructions;
import riscVivid.asm.instruction.Registers;

/*
 * TODO:
 * character literal
 * Error detection e.g.
 *  - 012abc
 *  - abc.+
 *  very ugly code but I learned much from it
 */
/**
 * This class reads Tokens from a BufferedReader
 * 
 */
public class Tokenizer {
	private BufferedCharReader reader_;
	private Token token_;
	private int char_;
	// ================* states *================
	private State startState;
	private State identifierState;
	private State labelState;
	private State directiveState;
//	private State operatorState;
	private State separatorState;
	private State signedConstantState;
	private State hexOctalConstantState;
	private State octalConstantState;
	private State hexConstantState;
	private State decimalConstantState;
	private State stringLiteralState;
	private State stringBackslashState;
	private State stringEndState;
	private State characterLiteralState;
	private State characterExpectingEndState;
	private State characterBackslashState;
	private State characterBackslashAfter1State;
	private State characterBackslashAfter2State;
	private State characterEndState;

	/**
	 * create new Tokenizer and initialize state machine
	 *
	 * @param reader
	 * @throws IOException
	 */
	public Tokenizer(BufferedReader reader) throws IOException {
		this();
		this.setReader(reader);
	}

	/**
	 * create new Tokenizer and initialize state machine
	 */
	 public Tokenizer() {

		startState = new State(false, "start");
		identifierState = new State(true, "identifier");
		labelState = new State(true, "label");
		directiveState = new State(false, "directive");
//		operatorState = new State(true, "operator");
		separatorState = new State(true, "separator");
		signedConstantState = new State(true, "signed constant");
		hexOctalConstantState = new State(true, "hex or octal constant");
		octalConstantState = new State(true, "octal constant");
		hexConstantState = new State(true, "hex constant");
		decimalConstantState = new State(true, "decimal constant");
		stringLiteralState = new State(false, "string literal");
		stringBackslashState = new State(false, "string backslash");
		stringEndState = new State(true, "string literal end");
		characterLiteralState = new State(false, "character literal");
		characterExpectingEndState = new State(false, "character expecting '");
		characterBackslashState = new State(false, "character backslash");
		characterBackslashAfter1State = new State(false, "character after backslash 1");
		characterBackslashAfter2State = new State(false, "character after backslash 2");
		characterEndState = new State(true, "character literal end");
		// ================* startState *================
		//leading whitespace
		startState.addTransition(new Transition(startState, new CharacterList(
				Properties.T_WHITESPACE)));
		//identifier
		startState.addTransition(new FunctionTransition(identifierState, new CharacterList(
				Properties.T_IDENTIFIER_START), new Procedure() {
			public void procedure(Object o) {
				startToken();
				setTokenType(TokenType.Identifier);
				appendChar();
			}
		}));
		//directive
		startState.addTransition(new FunctionTransition(directiveState, new Character('.'),
				new Procedure() {
					public void procedure(Object o) {
						startToken();
						setTokenType(TokenType.Directive);
						appendChar();
					}
				}));
/*
		//operator
		startState.addTransition(new FunctionTransition(operatorState, new CharacterList(
				Properties.T_OPERATOR), new Procedure() {
			public void procedure(Object o) {
				startToken();
				setTokenType(TokenType.Operator);
				appendChar();
			}
		}));
*/
		//separator
		startState.addTransition(new FunctionTransition(separatorState, new CharacterList(
				Properties.T_SEPARATOR), new Procedure() {
			public void procedure(Object o) {
				startToken();
				setTokenType(TokenType.Separator);
				appendChar();
			}
		}));

		// constant starting with sign
		startState.addTransition(new FunctionTransition(signedConstantState, 
			new CharacterList(new char[] {'+', '-'}), new Procedure() {
			public void procedure(Object o) {
				startToken();
				setTokenType(TokenType.Operator);
				appendChar();
			}
		}));

		//decimal constant
		startState.addTransition(new FunctionTransition(decimalConstantState, new CharacterList(
				Properties.T_DECIMAL_DIGIT_BEGIN), new Procedure() {
			public void procedure(Object o) {
				startToken();
				setTokenType(TokenType.IntegerConstant);
				appendChar();
			}
		}));
		//octal constant
		startState.addTransition(new FunctionTransition(hexOctalConstantState, new Character('0'),
				new Procedure() {
					public void procedure(Object o) {
						startToken();
						setTokenType(TokenType.IntegerConstant);
						appendChar();
					}
				}));
		//string literal
		startState.addTransition(new FunctionTransition(stringLiteralState, new Character('"'),
				new Procedure() {
					public void procedure(Object o) {
						startToken();
						setTokenType(TokenType.StringLiteral);
					}
				}));
		//character literal
		startState.addTransition(new FunctionTransition(characterLiteralState, new Character('\''),
				new Procedure() {
					public void procedure(Object o) {
						startToken();
						setTokenType(TokenType.CharacterLiteral);
					}
				}));
		// ================* identifierState *================
		identifierState.addTransition(new FunctionTransition(identifierState, new CharacterList(
				Properties.T_IDENTIFIER_PART), new Procedure() {
			public void procedure(Object o) {
				appendChar();
			}
		}));
		//label
		identifierState.addTransition(new FunctionTransition(labelState, new Character(':'),
				new Procedure() {
					public void procedure(Object o) {
						setTokenType(TokenType.Label);
						appendChar();
					}
				}));
		// ================* directiveState *================
		directiveState.addTransition(new FunctionTransition(identifierState, new CharacterList(
				Properties.T_IDENTIFIER_START), new Procedure() {
			public void procedure(Object o) {
				appendChar();
			}
		}));
		// ================* signedConstantState *================
		//decimal constant
		signedConstantState.addTransition(new FunctionTransition(decimalConstantState, 
			new CharacterList(Properties.T_DECIMAL_DIGIT_BEGIN), new Procedure() {
			public void procedure(Object o) {
				appendChar();
			}
		}));
		//octal constant
		signedConstantState.addTransition(new FunctionTransition(hexOctalConstantState, 
			new Character('0'), new Procedure() {
			public void procedure(Object o) {
				appendChar();
			}
		}));
		// ================* hexOctalConstantState *================
		hexOctalConstantState.addTransition(new FunctionTransition(octalConstantState,
				new CharacterList(Properties.T_OCTAL_DIGIT), new Procedure() {
					public void procedure(Object o) {
						appendChar();
					}
				}));
		char[] x = { 'X', 'x' };
		hexOctalConstantState.addTransition(new FunctionTransition(hexConstantState,
				new CharacterList(x), new Procedure() {
					public void procedure(Object o) {
						appendChar();
					}
				}));
		// ================* decimalConstantState *================
		decimalConstantState.addTransition(new FunctionTransition(decimalConstantState,
				new CharacterList(Properties.T_DECIMAL_DIGIT), new Procedure() {
					public void procedure(Object o) {
						appendChar();
					}
				}));
		// ================* octalConstantState *================
		octalConstantState.addTransition(new FunctionTransition(octalConstantState,
				new CharacterList(Properties.T_OCTAL_DIGIT), new Procedure() {
					public void procedure(Object o) {
						appendChar();
					}
				}));
		// ================* hexConstantState *================
		hexConstantState.addTransition(new FunctionTransition(hexConstantState, new CharacterList(
				Properties.T_HEX_DIGIT), new Procedure() {
			public void procedure(Object o) {
				appendChar();
			}
		}));
		// ================* stringLiteralState *================
		stringLiteralState.addTransition(new Transition(stringEndState, new Character('"')));
		stringLiteralState.addTransition(new Transition(stringBackslashState, new Character('\\')));
		char[] negChars = { '"', '\\' };
		stringLiteralState.addTransition(new FunctionTransition(stringLiteralState,
				new InverseCharacterList(negChars), new Procedure() {
					public void procedure(Object o) {
						appendChar();
					}
				}));
		stringBackslashState.addTransition(new FunctionTransition(stringLiteralState,
				new Character('"'), new Procedure() {
					public void procedure(Object o) {
						appendChar('"');
					}
				}));
		stringBackslashState.addTransition(new FunctionTransition(stringLiteralState,
				new InverseCharacterList('"'), new Procedure() {
					public void procedure(Object o) {
						appendChar('\\');
						appendChar();
					}
				}));
		// ================* characterLiteralState *================
		 char[] escapedChars = {'\'', '\\'};
		 characterLiteralState.addTransition(new FunctionTransition(characterExpectingEndState,
				 new InverseCharacterList(escapedChars), new Procedure() {
		 	public void procedure(Object o) {
		 		appendChar();
			}
		 }));
		 characterLiteralState.addTransition(new FunctionTransition(characterBackslashState,
				 new Character('\\'), new Procedure() {
		 	public void procedure(Object o) {
		 		appendChar();
			}
		 }));
		 characterExpectingEndState.addTransition(new Transition(characterEndState, new Character('\'')));
		 characterBackslashState.addTransition(new FunctionTransition(characterExpectingEndState,
				 new CharacterList(escapedChars), new Procedure() {
			 public void procedure(Object o) {
				 appendChar();
			 }
		 }));
		 characterBackslashState.addTransition(new FunctionTransition(characterExpectingEndState,
				 new Character('0'), new Procedure() {
			 public void procedure(Object o) {
				 appendChar();
			 }
		 }));
		 characterBackslashState.addTransition(new Transition(characterBackslashAfter1State,
				 new Character('x')));
		 characterBackslashAfter1State.addTransition(new FunctionTransition(characterBackslashAfter2State,
				new CharacterList(Properties.T_HEX_DIGIT), new Procedure() {
			 public void procedure(Object o) {
			 	 appendChar('x');
				 appendChar();
			 }
		 }));
		 characterBackslashAfter2State.addTransition(new FunctionTransition(characterExpectingEndState,
				 new CharacterList(Properties.T_HEX_DIGIT), new Procedure() {
			 public void procedure(Object o) {
				 appendChar();
			 }
		 }));
		 characterBackslashAfter2State.addTransition(new Transition(characterEndState, new Character('\'')));
	 }

	public void setReader(BufferedReader reader) throws IOException {
		if (reader == null)
			reader_ = null;
		else
			reader_ = new BufferedCharReader(reader);
	}

	/**
	 * 
	 * @return array of tokens of one line
	 * @throws IOException
	 * @throws TokenizerException
	 */
	public Token[] readLine() throws IOException, TokenizerException {
		if (reader_ == null || !reader_.readLine())
			return null;
		char_ = reader_.next();
		Vector<Token> tokens = new Vector<Token>();
		Token t = nextToken();
		while (t != null) {
			tokens.add(t);
			t = nextToken();
		}

		return tokens.toArray(new Token[0]);
	}

	/**
	 * 
	 * @return next token in line
	 * @throws TokenizerException
	 */
	private Token nextToken() throws TokenizerException {
		if (char_ == -1)
			return null;
		State currentState = startState;
		State lastState = null;
		token_ = null;

		lastState = currentState;
		currentState = currentState.doTransition(new Character((char) char_));
		if(currentState != null)
			char_ = reader_.next();

		while (currentState != null && char_ != -1) {
			lastState = currentState;
			currentState = currentState.doTransition(new Character((char) char_));
			if (currentState == null)
				break;
			char_ = reader_.next();
		}
		//TODO: problems with exceptions?
		if (token_ != null && currentState != null && !currentState.isAccepting()) {
			throw new TokenizerException("unexpected end of token'", reader_.position());
		}
		if (char_ != -1 && !lastState.isAccepting())
			throw new TokenizerException("not expected character: '" + (char) char_ + "'",
					reader_.position());

		if (token_ != null && token_.getTokenType() == TokenType.Identifier)
			setIdentifierType(token_);
		if (token_ != null && token_.getTokenType() == TokenType.Label && token_.getString().charAt(0) == '.') {
			throw new TokenizerException("Label cannot start with a period", new Position(
					reader_.position().line, 0));
		}
		return token_;
	}

	private void setIdentifierType(Token t) {
		if (Registers.instance().getInteger(t.getString()) != null)
			t.setTokenType(TokenType.Register);
		else if (Instructions.instance().getInstruction(t.getString()) != null) {
			t.setTokenType(TokenType.Mnemonic);
		}
	}

	/*
	 * ===============================* Wrapper *===============================
	 */
	private void startToken() {
		token_ = new Token(reader_.position());
	}

	private void appendChar() {
		token_.append((char) char_);
	}

	private void appendChar(char c) {
		token_.append(c);
	}

	private void setTokenType(TokenType t) {
		token_.setTokenType(t);
	}
}
