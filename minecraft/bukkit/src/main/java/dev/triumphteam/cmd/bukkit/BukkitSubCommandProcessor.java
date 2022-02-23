/**
 * MIT License
 * <p>
 * Copyright (c) 2019-2021 Matt
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.cmd.bukkit;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.bukkit.message.NoPermissionMessageContext;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.argument.ArgumentRegistry;
import dev.triumphteam.cmd.core.exceptions.SubCommandRegistrationException;
import dev.triumphteam.cmd.core.message.MessageRegistry;
import dev.triumphteam.cmd.core.processor.AbstractSubCommandProcessor;
import dev.triumphteam.cmd.core.requirement.Requirement;
import dev.triumphteam.cmd.core.requirement.RequirementRegistry;
import dev.triumphteam.cmd.core.sender.SenderValidator;
import dev.triumphteam.cmd.core.suggestion.Suggestion;
import dev.triumphteam.cmd.core.suggestion.SuggestionRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

final class BukkitSubCommandProcessor<S> extends AbstractSubCommandProcessor<S> {

    private final List<Requirement<CommandSender, ?>> defaultRequirements = new ArrayList<>();
    private final List<Suggestion<S>> suggestions = new ArrayList<>();

    public BukkitSubCommandProcessor(
            @NotNull final BaseCommand baseCommand,
            @NotNull final String parentName,
            @NotNull final Method method,
            @NotNull final ArgumentRegistry<S> argumentRegistry,
            @NotNull final RequirementRegistry<S> requirementRegistry,
            @NotNull final MessageRegistry<S> messageRegistry,
            @NotNull final SuggestionRegistry<S> suggestionRegistry,
            @NotNull final SenderValidator<S> senderValidator
    ) {
        super(baseCommand, parentName, method, argumentRegistry, requirementRegistry, messageRegistry, senderValidator);
        if (getName() == null) return;
        suggestions.addAll(SuggestionRegistry.extractSuggestions(suggestionRegistry, method, baseCommand.getClass()));
        checkPermission(getMethod());
    }

    @NotNull
    public List<Suggestion<S>> getSuggestions() {
        return suggestions;
    }

    /**
     * Gets the default requirements for this sub command.
     *
     * @return The default requirements for this sub command.
     */
    public List<Requirement<CommandSender, ?>> getDefaultRequirements() {
        return defaultRequirements;
    }

    // TODO: 2/4/2022 comments
    private void checkPermission(@NotNull final Method method) {
        final Permission permission = method.getAnnotation(Permission.class);
        if (permission == null) return;

        final String annotatedPermission = permission.value();

        if (annotatedPermission.isEmpty()) {
            throw new SubCommandRegistrationException("Permission cannot be empty", method, getBaseCommand().getClass());
        }

        defaultRequirements.add(
                new Requirement<>(
                        sender -> sender.hasPermission(annotatedPermission),
                        BukkitMessageKey.NO_PERMISSION,
                        (command, subCommand) -> new NoPermissionMessageContext(command, subCommand, annotatedPermission),
                        false
                )
        );
    }
}
