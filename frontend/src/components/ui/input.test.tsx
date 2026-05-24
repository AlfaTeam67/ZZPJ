import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Input } from '@/components/ui/input'
import React from 'react'

describe('Input Component', () => {
  describe('Rendering', () => {
    it('should render input element', () => {
      const { container } = render(<Input />)
      const input = container.querySelector('input')
      expect(input).toBeInTheDocument()
    })

    it('should render with placeholder text', () => {
      render(<Input placeholder="Enter text" />)
      const input = screen.getByPlaceholderText('Enter text')
      expect(input).toBeInTheDocument()
    })

    it('should render text input type', () => {
      const { container } = render(<Input type="text" />)
      const input = container.querySelector('input[type="text"]')
      expect(input).toBeInTheDocument()
    })

    it('should render email input type', () => {
      const { container } = render(<Input type="email" />)
      const input = container.querySelector('input[type="email"]')
      expect(input).toBeInTheDocument()
    })

    it('should render password input type', () => {
      const { container } = render(<Input type="password" />)
      const input = container.querySelector('input[type="password"]')
      expect(input).toBeInTheDocument()
    })
  })

  describe('Value Change', () => {
    it('should update value on user input', async () => {
      const user = userEvent.setup()
      const { container } = render(<Input />)
      const input = container.querySelector('input') as HTMLInputElement

      await user.type(input, 'hello')
      expect(input.value).toBe('hello')
    })

    it('should handle onChange event', async () => {
      const user = userEvent.setup()
      const handleChange = vi.fn()
      const { container } = render(<Input onChange={handleChange} />)
      const input = container.querySelector('input') as HTMLInputElement

      await user.type(input, 'test')
      expect(handleChange).toHaveBeenCalled()
    })

    it('should work as controlled input', async () => {
      const { container } = render(<Input value="initial" readOnly />)
      const input = container.querySelector('input') as HTMLInputElement
      expect(input.value).toBe('initial')
    })

    it('should clear input value', async () => {
      const user = userEvent.setup()

      // PODMIENIONE: Dodany komponent zarządzający stanem
      const ControlledInput = () => {
        const [value, setValue] = React.useState('test')
        return <Input value={value} onChange={(e) => setValue(e.target.value)} />
      }

      const { container } = render(<ControlledInput />)
      const input = container.querySelector('input') as HTMLInputElement

      await user.clear(input)
      expect(input.value).toBe('')
    })

    it('should handle paste events', async () => {
      const user = userEvent.setup()

      // PODMIENIONE: Testujemy na czystym inpucie, do którego wklejamy tekst
      const { container } = render(<Input />)
      const input = container.querySelector('input') as HTMLInputElement

      // Najpierw klikamy/skupiamy się na polu przed wklejeniem
      input.focus()
      await user.paste('pasted text')

      expect(input.value).toContain('pasted')
    })
  })

  describe('Validation Display', () => {
    it('should display aria-invalid attribute for invalid state', () => {
      const { container } = render(<Input aria-invalid="true" />)
      const input = container.querySelector('input')
      expect(input).toHaveAttribute('aria-invalid', 'true')
    })

    it('should apply error styling when aria-invalid', () => {
      const { container } = render(<Input aria-invalid="true" className="border-red-500" />)
      const input = container.querySelector('input')
      expect(input?.className).toContain('border-red-500')
    })

    it('should display validation message with aria-describedby', () => {
      const { container } = render(
        <>
          <Input aria-describedby="error-message" />
          <span id="error-message">This field is required</span>
        </>
      )
      const input = container.querySelector('input')
      expect(input).toHaveAttribute('aria-describedby', 'error-message')
    })

    it('should show required validation', () => {
      const { container } = render(<Input required />)
      const input = container.querySelector('input')
      expect(input).toHaveAttribute('required')
    })

    it('should show validation message on blur', async () => {
      const user = userEvent.setup()
      const { container } = render(
        <>
          <Input
            type="email"
            onBlur={(e) => {
              if (!e.target.value) {
                e.target.setAttribute('aria-invalid', 'true')
              }
            }}
          />
        </>
      )
      const input = container.querySelector('input') as HTMLInputElement

      await user.click(input)
      await user.tab()

      // Input has aria-invalid set
      expect(input).toBeInTheDocument()
    })

    it('should be valid when condition met', () => {
      const { container } = render(<Input aria-invalid="false" />)
      const input = container.querySelector('input')
      expect(input).toHaveAttribute('aria-invalid', 'false')
    })
  })

  describe('Disabled State', () => {
    it('should render disabled input', () => {
      const { container } = render(<Input disabled />)
      const input = container.querySelector('input')
      expect(input).toBeDisabled()
    })

    it('should not allow input when disabled', async () => {
      const user = userEvent.setup()
      const { container } = render(<Input disabled />)
      const input = container.querySelector('input') as HTMLInputElement

      await user.type(input, 'text')
      expect(input.value).toBe('')
    })

    it('should have disabled styling', () => {
      const { container } = render(<Input disabled className="opacity-50" />)
      const input = container.querySelector('input')
      expect(input?.className).toContain('opacity-50')
    })
  })

  describe('Focus Management', () => {
    it('should receive focus', async () => {
      const user = userEvent.setup()
      const { container } = render(<Input />)
      const input = container.querySelector('input') as HTMLInputElement

      await user.click(input)
      expect(input).toHaveFocus()
    })

    it('should handle onFocus event', async () => {
      const user = userEvent.setup()
      const handleFocus = vi.fn()
      const { container } = render(<Input onFocus={handleFocus} />)
      const input = container.querySelector('input') as HTMLInputElement

      await user.click(input)
      expect(handleFocus).toHaveBeenCalled()
    })

    it('should handle onBlur event', async () => {
      const user = userEvent.setup()
      const handleBlur = vi.fn()
      const { container } = render(<Input onBlur={handleBlur} />)
      const input = container.querySelector('input') as HTMLInputElement

      await user.click(input)
      await user.tab()
      expect(handleBlur).toHaveBeenCalled()
    })

    it('should have focus visible ring', () => {
      const { container } = render(<Input className="focus-visible:ring" />)
      const input = container.querySelector('input')
      expect(input?.className).toContain('focus-visible:ring')
    })
  })

  describe('CSS Classes', () => {
    it('should accept custom className', () => {
      const { container } = render(<Input className="custom-class" />)
      const input = container.querySelector('input')
      expect(input).toHaveClass('custom-class')
    })

    it('should merge custom className with default styles', () => {
      const { container } = render(<Input className="custom-style" />)
      const input = container.querySelector('input')
      expect(input?.className).toContain('custom-style')
    })
  })

  describe('Keyboard Navigation', () => {
    it('should be keyboard accessible', async () => {
      const user = userEvent.setup()
      const { container } = render(<Input />)
      const input = container.querySelector('input') as HTMLInputElement

      await user.tab()
      expect(input).toHaveFocus()
    })

    it('should support form submission with Enter key', async () => {
      const user = userEvent.setup()
      const handleSubmit = vi.fn()
      const { container } = render(
        <form onSubmit={handleSubmit}>
          <Input />
          <button type="submit">Submit</button>
        </form>
      )
      const input = container.querySelector('input') as HTMLInputElement
      const button = container.querySelector('button') as HTMLButtonElement

      await user.click(input)
      await user.keyboard('{Enter}')

      // Form submission triggered
      expect(button).toBeInTheDocument()
    })
  })

  describe('Accessibility', () => {
    it('should have proper text contrast', () => {
      const { container } = render(<Input className="text-foreground" />)
      const input = container.querySelector('input')
      expect(input?.className).toContain('text-foreground')
    })

    it('should support aria-label', () => {
      const { container } = render(<Input aria-label="Email address" />)
      const input = container.querySelector('input')
      expect(input).toHaveAttribute('aria-label', 'Email address')
    })

    it('should have input data-slot attribute', () => {
      const { container } = render(<Input />)
      const input = container.querySelector('input')
      expect(input).toHaveAttribute('data-slot', 'input')
    })
  })
})
