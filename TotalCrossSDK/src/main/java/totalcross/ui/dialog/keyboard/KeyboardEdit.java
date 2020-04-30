// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.dialog.keyboard;

import totalcross.ui.Edit;

public class KeyboardEdit extends Edit {
  /** Sinaliza se o teclado está acionado para todos os Edits. */
  protected static boolean keyboardOn = true;
  /** tamanho maximo. */
  protected Integer maxLength2 = null;
  /** Janela do teclado. */
  protected VirtualKeyboard teclado = null;

  /**
   * Construtor.
   *
   * @param getMask() Máscara.
   */
  public KeyboardEdit(final String mask) {
    super(mask);
  }

  /** Construtor padrão. */
  public KeyboardEdit() {}

  /**
   * Método que seta o texto que será exibido no edit.
   *
   * @param value Texto.
   */
  @Override
  public void setText(String value) {
    if (value == null) {
      value = "";
    }
    super.setText(value);
  }

  /**
   * Método que retorna o valor que está sendo exibido. o Valor é retornado com os espaços em branco
   * dos lados do valor, removidos (trim()).
   *
   * @return String Valor exibido.
   */
  @Override
  public String getText() {
    return super.getText().trim();
  }

  /**
   * Método que retorna o valor que está sendo exibido. o Valor é retornado com os espaços em branco
   * dos lados do valor.
   *
   * @return String Valor exibido.
   */
  public String getTextFull() {
    return super.getText();
  }

  /** Seta o tamanho maximo que esse campo pode ter */
  @Override
  public void setMaxLength(int length) {
    maxLength2 = length;
    super.setMaxLength(length);
  }

  /**
   * Mudando a visibilidade do Método para public. Esse Método verifica se um caractere pode ser
   * escrito nesse determinado edit
   */
  @Override
  public boolean isCharValid(char c) {
    return super.isCharValid(c);
  }

  public void setCursorPosEnd() {
    final int length = getLength();
    setCursorPos(length, length);
  }
}
