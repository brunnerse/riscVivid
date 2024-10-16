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
package riscVivid.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.util.Properties;

import javax.swing.*;
import javax.swing.text.*;
import java.text.NumberFormat;

import riscVivid.BranchPredictionModule;
import riscVivid.datatypes.ArchCfg;
import riscVivid.datatypes.BranchPredictorState;
import riscVivid.datatypes.BranchPredictorType;
import riscVivid.gui.GUI_CONST;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.userLevel.CommandSetInitialize;
import riscVivid.gui.util.DialogWrapper;

@SuppressWarnings("serial")
public class OptionDialog extends JDialog implements ActionListener, ItemListener, KeyEventDispatcher
{
    // two control buttons, press confirm to save selected options
    private JButton confirm;
    private JButton cancel;

    // checkBoxes
    private JCheckBox forwardingCheckBox;
    private JCheckBox mipsCompatibilityCheckBox;
    private JCheckBox noBranchDelaySlotCheckBox;
    private JCheckBox memoryWarningCheckBox;
    private JCheckBox initializationWarningCheckBox;
    private JCheckBox customRegisterOrderCheckBox;
    /*
     * JComboBox may be represented by Vectors or Arrays of Objects (Object [])
     * we have chosen "String[]" to be the representation (in fact - String) for
     * the data within AsmFileLoader-class , but Vector is appropriate as well.
     */
    private JComboBox<String> bpTypeComboBox;
    private JComboBox<String> bpInitialStateComboBox;
    private JTextField btbSizeTextField;

    private JComboBox<String> initRegisterComboBox;
    private JComboBox<String> initMemoryComboBox;

    private JComboBox<String> numBranchDelaySlotsComboBox;

    //input text fields
    private JFormattedTextField maxCyclesTextField;

    public OptionDialog(Frame owner)
    {
        //calls modal constructor, set to "false" to make dialog non-modal
        super(owner, true);
        setLayout(new BorderLayout());
        setTitle("Options");
        //control buttons
        confirm = new JButton("OK");
        confirm.addActionListener(this);
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        //the panel containing all the control buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirm);
        buttonPanel.add(cancel);
        add(buttonPanel, BorderLayout.SOUTH);

        /*instantiate all the components that you'd like to use as input,
         * as well as any labels describing them, HERE: */

        /*create a checkboxes
         *
         * checkboxes don't need a label -> the name is part of the constructor
         *-> its a single element, hence it doesn't need a JPanel  */
        forwardingCheckBox = new JCheckBox("Use Forwarding");
        forwardingCheckBox.setSelected(Preference.pref.getBoolean(Preference.forwardingPreferenceKey, true)); // load current value
        forwardingCheckBox.setToolTipText("Forwards results from the pipeline to the inputs of the Execute Stage");

        noBranchDelaySlotCheckBox = new JCheckBox("Clear Branch Delay Slots");
        noBranchDelaySlotCheckBox.setSelected(Preference.pref.getBoolean(Preference.noBranchDelaySlotPreferenceKey, true)); // load current value
        noBranchDelaySlotCheckBox.setToolTipText("Aborts the execution of the instructions after a jump if the jump is taken");

        mipsCompatibilityCheckBox = new JCheckBox("MIPS Compatibility Mode (requires Forwarding)");
        mipsCompatibilityCheckBox.setSelected(Preference.pref.getBoolean(Preference.mipsCompatibilityPreferenceKey, true)); // load current value
        mipsCompatibilityCheckBox.setToolTipText("Inserts a pipeline bubble after a load command if the subsequent instruction reads the target register, as forwarding is not possible in this case");

        // disable MIPS compatibility if no forwarding is active
        if (!forwardingCheckBox.isSelected())
        {
            mipsCompatibilityCheckBox.setSelected(false);
        }
        forwardingCheckBox.addItemListener(this);
        mipsCompatibilityCheckBox.addItemListener(this);
        noBranchDelaySlotCheckBox.addItemListener(this);

        memoryWarningCheckBox = new JCheckBox("Enable Unreserved Memory Warnings");
        memoryWarningCheckBox.setSelected(Preference.isMemoryWarningsEnabled());
        memoryWarningCheckBox.setToolTipText("Pauses the execution when accessing an unreserved memory address");

        initializationWarningCheckBox = new JCheckBox("Enable Warnings for Reading Uninitialized Registers");
        initializationWarningCheckBox.setSelected(Preference.isInitializationWarningsEnabled());
        initializationWarningCheckBox.setToolTipText("Pauses the execution when reading a register that has not been previously written to");

        customRegisterOrderCheckBox = new JCheckBox("Use custom order for the register set");
        customRegisterOrderCheckBox.setSelected(Preference.useCustomRegisterOrder());
        customRegisterOrderCheckBox.setToolTipText("Sorts the registers in the register set frame in a more intuitive order");
        /*create a JComboBoxes
         *
         * JComboBox need a Object[] or Vector as data representation
         Furthermore the  JComboBox gets a JLabel, describing it,
         * -> put both components into a JPanel*/

        // bpType:
        JLabel bpTypeComboBoxDescriptionLabel = new JLabel("Branch Predictor: ");
        bpTypeComboBox = new JComboBox<String>(BranchPredictorType.getValuesGuiStrings());
        BranchPredictorType selectedType = ArchCfg.getBranchPredictorTypeFromString(
                Preference.pref.get(Preference.bpTypePreferenceKey, ""));
        bpTypeComboBox.setSelectedItem(selectedType.toGuiString()); // load current value
        //surrounding panel
        JPanel bpTypeListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //add the label
        bpTypeListPanel.add(bpTypeComboBoxDescriptionLabel);
        //add the box itself
        bpTypeListPanel.add(bpTypeComboBox);

        // bpInitialState:
        JLabel bpInitialStateComboBoxDescriptionLabel = new JLabel("Initial Predictor State: ");
        bpInitialStateComboBox = new JComboBox<String>(BranchPredictorState.getValuesGuiStrings());
        bpInitialStateComboBox.setSelectedItem(ArchCfg.getBranchPredictorInitialStateFromString(
                Preference.pref.get(Preference.bpInitialStatePreferenceKey, BranchPredictorState.UNKNOWN.toString())).toGuiString()); // load current value
        //surrounding panel
        JPanel bpInitialStateListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //add the label
        bpInitialStateListPanel.add(bpInitialStateComboBoxDescriptionLabel);
        //add the box itself
        bpInitialStateListPanel.add(bpInitialStateComboBox);

        /*create a  JTextFields
         * the field and a JLabel description
         */

        // Max Cycles
        JLabel maxCyclesTextFieldDescription = new JLabel("Maximum Cycles: ");
        // the number in constructor means the number of lines in textfield
        NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        numberFormat.setGroupingUsed(false);
        NumberFormatter numberFormatter = new NumberFormatter(numberFormat);
        numberFormatter.setAllowsInvalid(false);
        numberFormatter.setMinimum(0);
        numberFormatter.setMaximum(99999999);

        maxCyclesTextField = new JFormattedTextField(numberFormatter);
        maxCyclesTextField.setColumns(8);
        //load current text from ArchCfg
        maxCyclesTextField.setText((Integer.valueOf(ArchCfg.getMaxCycles())).toString());
        //surrounding panel, containing both JLabel and JTextField
        JPanel maxCyclesTextFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //add the label
        maxCyclesTextFieldPanel.add(maxCyclesTextFieldDescription);
        //add the field itself
        maxCyclesTextFieldPanel.add(maxCyclesTextField);
        maxCyclesTextFieldPanel.setToolTipText("Stops the simulation after this amount of cycles to prevent infinite execution");

        // BTB Size
        JLabel btbSizeTextFieldDescription = new JLabel("BTB Size: ");
        // the number in constructor means the number of lines in textfield
        btbSizeTextField = new JTextField(5);
        //load current text from ArchCfg
        btbSizeTextField.setText((Integer.valueOf(ArchCfg.getBranchPredictorTableSize())).toString());
        //surrounding panel, containing both JLabel and JTextField
        JPanel btbSizeTextFieldPanel = new JPanel();
        //add the label
        btbSizeTextFieldPanel.add(btbSizeTextFieldDescription);
        //add the field itself
        btbSizeTextFieldPanel.add(btbSizeTextField);

        // Initial values:
        JLabel initRegisterComboBoxDescriptionLabel = new JLabel("Initial Value of Register Bytes: ");
        initRegisterComboBox = new JComboBox<String>();
        //surrounding panel
        JPanel initRegisterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //add the label
        initRegisterPanel.add(initRegisterComboBoxDescriptionLabel);
        //add the box itself
        initRegisterPanel.add(initRegisterComboBox);

        JLabel initMemoryComboBoxDescriptionLabel = new JLabel("Initial Value of Memory Bytes: ");
        initMemoryComboBox = new JComboBox<String>();
        //surrounding panel
        JPanel initMemoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //add the label
        initMemoryPanel.add(initMemoryComboBoxDescriptionLabel);
        //add the box itself
        initMemoryPanel.add(initMemoryComboBox);

        for (CommandSetInitialize.Choice c : CommandSetInitialize.Choice.values())
        {
            initRegisterComboBox.addItem(CommandSetInitialize.getChoiceString(c));
            if (CommandSetInitialize.getChoiceInt(c) == Preference.pref.getInt(Preference.initializeRegistersPreferenceKey,
                    CommandSetInitialize.getChoiceInt(CommandSetInitialize.Choice.ZERO)))
                initRegisterComboBox.setSelectedIndex(initRegisterComboBox.getItemCount()-1);

            initMemoryComboBox.addItem(CommandSetInitialize.getChoiceString(c));
            if (CommandSetInitialize.getChoiceInt(c) == Preference.pref.getInt(Preference.initializeMemoryPreferenceKey,
                    CommandSetInitialize.getChoiceInt(CommandSetInitialize.Choice.ZERO)))
                initMemoryComboBox.setSelectedIndex(initMemoryComboBox.getItemCount()-1);
        }


        JLabel numBranchSlotsDescriptionLabel = new JLabel("Number of Branch Delay Slots: ");
        numBranchDelaySlotsComboBox = new JComboBox<String>(new String[] {"2", "3"});
        numBranchDelaySlotsComboBox.setSelectedItem(
                Preference.pref.get(Preference.numBranchDelaySlotsPreferenceKey, "3"));
        //surrounding panel
        JPanel branchDelaySlotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //add the label
        branchDelaySlotsPanel.add(numBranchSlotsDescriptionLabel);
        //add the box itself
        branchDelaySlotsPanel.add(numBranchDelaySlotsComboBox);

        //this panel contains all input components = top level panel
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new GridLayout(0, 1));

        //dont forget adding the components to the panel !!!
        for (JComponent c : new JComponent[]{noBranchDelaySlotCheckBox, forwardingCheckBox, mipsCompatibilityCheckBox,
                memoryWarningCheckBox, initializationWarningCheckBox, customRegisterOrderCheckBox,
                // bpTypeListPanel, bpInitialStateListPanel, btbSizeTextFieldPanel, // TODO: add again when branch prediction works correctly
                maxCyclesTextFieldPanel, initRegisterPanel, initMemoryPanel, branchDelaySlotsPanel})
        {
            optionPanel.add(c);
            c.setFont(c.getFont().deriveFont((float)Preference.getFontSize()));
            for (Component child : c.getComponents())
               child.setFont(child.getFont().deriveFont((float)Preference.getFontSize()));
        }
        for (JComponent c : new JComponent[]{confirm, cancel})
            c.setFont(c.getFont().deriveFont((float)Preference.getFontSize()));

        //adds the top-level-panel to the Dialog frame
        add(optionPanel, BorderLayout.CENTER);

        //dialog appears in the middle of the MainFrame
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);

        // add Listener
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        setVisible(true);
    }

    @Override
    public void dispose() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
        super.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        //just close the dialog
        if (e.getSource().equals(cancel))
        {
            setVisible(false);
            dispose();
        }

        /* get all the values, assign them to data within ArchCfg
         * and save them as preference for future use
         */
        if (e.getSource().equals(confirm))
        {
            Preference.pref.putBoolean(Preference.forwardingPreferenceKey,
                    forwardingCheckBox.isSelected());
            Preference.pref.putBoolean(Preference.noBranchDelaySlotPreferenceKey,
            		noBranchDelaySlotCheckBox.isSelected());
            // only enable mips if forwarding and noBranchdelaySlot is enabled
            Preference.pref.putBoolean(Preference.mipsCompatibilityPreferenceKey,
                    mipsCompatibilityCheckBox.isSelected() && forwardingCheckBox.isSelected());
            Preference.pref.putBoolean(Preference.enableMemoryWarningsPreferenceKey,
                    memoryWarningCheckBox.isSelected());
            Preference.pref.putBoolean(Preference.enableInitializationWarningsPreferenceKey,
                    initializationWarningCheckBox.isSelected());
            Preference.pref.putBoolean(Preference.enableInitializationWarningsPreferenceKey,
                    initializationWarningCheckBox.isSelected());
            Preference.pref.putBoolean(Preference.useCustomRegisterOrderPreferenceKey,
                    customRegisterOrderCheckBox.isSelected());

            boolean no_branch_delay_slot, use_load_stall_bubble, use_forwarding;

            no_branch_delay_slot = noBranchDelaySlotCheckBox.isSelected();
            use_forwarding = forwardingCheckBox.isSelected();

            if (use_forwarding)
            {
                use_load_stall_bubble = mipsCompatibilityCheckBox.isSelected();
            }
            else
            {
                use_load_stall_bubble = false;

                if(mipsCompatibilityCheckBox.isSelected())
                {
                    // reset the MIPS compatibility
                    mipsCompatibilityCheckBox.setSelected(false);
                    Preference.pref.putBoolean(Preference.mipsCompatibilityPreferenceKey, false);

                    DialogWrapper.showMessageDialog(MainFrame.getInstance(), "Reset \"MIPS compatibility mode\", since it requires activated forwarding.", "Info",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }

            // TODO also add a field for disabling the branch prediction
            // TODO do some checks for the setting of the BP initial state and sizes

            BranchPredictorType branch_predictor_type = ArchCfg.getBranchPredictorTypeFromGuiString(bpTypeComboBox.getSelectedItem().toString());
            Preference.pref.put(Preference.bpTypePreferenceKey, branch_predictor_type.toString());

            BranchPredictorState branch_predictor_initial_state = ArchCfg.
                    getBranchPredictorInitialStateFromGuiString(
                            bpInitialStateComboBox.getSelectedItem().toString());
            Preference.pref.put(Preference.bpInitialStatePreferenceKey,
                    branch_predictor_initial_state.toString());

            int branch_predictor_table_size = Integer.parseInt(btbSizeTextField.getText());
            Preference.pref.put(Preference.btbSizePreferenceKey, btbSizeTextField.getText());

            // correct user input

            switch(branch_predictor_type)
            {
            case UNKNOWN:
            case S_ALWAYS_TAKEN:
            case S_ALWAYS_NOT_TAKEN:
            case S_BACKWARD_TAKEN:
                // unknown and static predictors have no initial state and no branch predictor table size
                branch_predictor_initial_state = BranchPredictorState.UNKNOWN;
                Preference.pref.put(Preference.bpInitialStatePreferenceKey,
                        BranchPredictorState.UNKNOWN.toString());

                branch_predictor_table_size = 1;
                Preference.pref.put(Preference.btbSizePreferenceKey,
                        Integer.valueOf(branch_predictor_table_size).toString());
                break;
            case D_1BIT:
                switch(branch_predictor_initial_state)
                {
                case PREDICT_STRONGLY_NOT_TAKEN:
                case PREDICT_WEAKLY_NOT_TAKEN:
                    // correct 2bit states to 1 bit state
                    branch_predictor_initial_state = BranchPredictorState.PREDICT_NOT_TAKEN;
                    Preference.pref.put(Preference.bpInitialStatePreferenceKey,
                            branch_predictor_initial_state.toString());
                    break;
                case PREDICT_STRONGLY_TAKEN:
                case PREDICT_WEAKLY_TAKEN:
                    // correct 2bit states to 1 bit state
                    branch_predictor_initial_state = BranchPredictorState.PREDICT_TAKEN;
                    Preference.pref.put(Preference.bpInitialStatePreferenceKey,
                            branch_predictor_initial_state.toString());
                    break;
                case UNKNOWN:
                default:
                    branch_predictor_initial_state = BranchPredictorState.PREDICT_NOT_TAKEN;
                    Preference.pref.put(Preference.bpInitialStatePreferenceKey,
                            branch_predictor_initial_state.toString());
                    // TODO Throw exception
                    break;
                }
                break;

            case D_2BIT_SATURATION:
            case D_2BIT_HYSTERESIS:
                switch(branch_predictor_initial_state)
                {
                case PREDICT_NOT_TAKEN:
                    // correct 1bit states to 2 bit state
                    branch_predictor_initial_state = BranchPredictorState.PREDICT_WEAKLY_NOT_TAKEN;
                    Preference.pref.put(Preference.bpInitialStatePreferenceKey,
                            branch_predictor_initial_state.toString());
                    break;
                case PREDICT_TAKEN:
                    // correct 1bit states to 2 bit state
                    branch_predictor_initial_state = BranchPredictorState.PREDICT_WEAKLY_TAKEN;
                    Preference.pref.put(Preference.bpInitialStatePreferenceKey,
                        branch_predictor_initial_state.toString());
                    break;
                case UNKNOWN:
                default:
                    branch_predictor_initial_state = BranchPredictorState.PREDICT_WEAKLY_NOT_TAKEN;
                    Preference.pref.put(Preference.bpInitialStatePreferenceKey,
                            branch_predictor_initial_state.toString());
                    // TODO Throw exception
                    break;
                }
                break;
            }

            // the btb has to be a power of two
            if (branch_predictor_table_size == 0)
            {
                branch_predictor_table_size = 1;
                Preference.pref.put(Preference.btbSizePreferenceKey, (Integer.valueOf(branch_predictor_table_size)).toString());
                // TODO Throw exception
            }

            int max_cycles;
            try {
                max_cycles = Integer.parseInt(maxCyclesTextField.getText());
                Preference.pref.put(Preference.maxCyclesPreferenceKey, maxCyclesTextField.getText());
            } catch (NumberFormatException ex) {
                max_cycles = ArchCfg.getMaxCycles();
                System.err.println("Error in max_cycles field. Ignoring max_cycles input.");
            }

            Preference.pref.putInt(Preference.initializeRegistersPreferenceKey,
                    CommandSetInitialize.getChoiceInt(initRegisterComboBox.getSelectedItem().toString()));
            Preference.pref.putInt(Preference.initializeMemoryPreferenceKey,
                    CommandSetInitialize.getChoiceInt(initMemoryComboBox.getSelectedItem().toString()));
            Preference.pref.put(Preference.numBranchDelaySlotsPreferenceKey,
                    numBranchDelaySlotsComboBox.getSelectedItem().toString());
            int num_branch_delay_slots = Integer.decode(numBranchDelaySlotsComboBox.getSelectedItem().toString());


            Properties config = new Properties();
            config.setProperty("isa_type", ArchCfg.getISAType().toString());
            config.setProperty("no_branch_delay_slot", String.valueOf(no_branch_delay_slot));
            config.setProperty("use_forwarding", String.valueOf(use_forwarding));
            config.setProperty("use_load_stall_bubble", String.valueOf(use_load_stall_bubble));
            config.setProperty("num_branch_delay_slots", String.valueOf(num_branch_delay_slots));
            config.setProperty("btb_predictor", branch_predictor_type.toString());
            config.setProperty("btb_predictor_initial_state", branch_predictor_initial_state.toString());
            config.setProperty("btb_size", String.valueOf(branch_predictor_table_size));
            config.setProperty("max_cycles", String.valueOf(max_cycles));
            ArchCfg.registerArchitectureConfig(config);

            // if simulator was started, display message that simulator needs to be restarted in order to apply the new settings
            if (MainFrame.getInstance().getOpenDLXSimState() != GUI_CONST.OpenDLXSimState.IDLE &&
                    Preference.pref.getBoolean(Preference.showInitializeOptionMessage, true))
            {
                final String message = "In order to apply the new settings, the program must be reassembled.";
                final JCheckBox checkbox = new JCheckBox("Do not show this message again.");
                final Object content[] = {message, checkbox};
                DialogWrapper.showWarningDialog(MainFrame.getInstance(), content, "Reassemble to apply settings");

                Preference.pref.putBoolean(Preference.showInitializeOptionMessage, !checkbox.isSelected());
            }

            setVisible(false);
            dispose();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == forwardingCheckBox) {
           if (!forwardingCheckBox.isSelected()) {
               if (mipsCompatibilityCheckBox.isSelected())
                   mipsCompatibilityCheckBox.setSelected(false);
           }
           else if (forwardingCheckBox.isSelected() && !mipsCompatibilityCheckBox.isSelected()) {
               mipsCompatibilityCheckBox.setSelected(true);
           }
        } else if (e.getSource() == mipsCompatibilityCheckBox) {
            if (mipsCompatibilityCheckBox.isSelected() && !forwardingCheckBox.isSelected()) {
                forwardingCheckBox.setSelected(true);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                cancel.doClick();
                return true;
            }
            else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                if (this.isVisible())
                    confirm.doClick();
                return true;
            }
        }
        return false;
    }
}
