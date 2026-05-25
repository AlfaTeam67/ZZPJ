import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from '@/components/ui/card'

describe('Card Components', () => {
  describe('Card', () => {
    it('should render card element', () => {
      const { container } = render(
        <Card>
          <div>Content</div>
        </Card>
      )
      const card = container.querySelector('[data-slot="card"]')
      expect(card).toBeInTheDocument()
    })

    it('should render card with children', () => {
      render(
        <Card>
          <p>Test content</p>
        </Card>
      )
      expect(screen.getByText('Test content')).toBeInTheDocument()
    })

    it('should apply default size', () => {
      const { container } = render(<Card>Content</Card>)
      const card = container.querySelector('[data-slot="card"]')
      expect(card).toHaveAttribute('data-size', 'default')
    })

    it('should apply small size variant', () => {
      const { container } = render(<Card size="sm">Content</Card>)
      const card = container.querySelector('[data-slot="card"]')
      expect(card).toHaveAttribute('data-size', 'sm')
    })

    it('should apply custom className', () => {
      const { container } = render(<Card className="custom-class">Content</Card>)
      const card = container.querySelector('[data-slot="card"]')
      expect(card).toHaveClass('custom-class')
    })

    it('should support multiple children', () => {
      render(
        <Card>
          <p>First</p>
          <p>Second</p>
          <p>Third</p>
        </Card>
      )
      expect(screen.getByText('First')).toBeInTheDocument()
      expect(screen.getByText('Second')).toBeInTheDocument()
      expect(screen.getByText('Third')).toBeInTheDocument()
    })

    it('should have proper styling classes', () => {
      const { container } = render(<Card>Content</Card>)
      const card = container.querySelector('[data-slot="card"]')
      expect(card?.className).toContain('rounded-2xl')
      expect(card?.className).toContain('bg-card')
    })
  })

  describe('CardHeader', () => {
    it('should render card header', () => {
      const { container } = render(
        <Card>
          <CardHeader>Header content</CardHeader>
        </Card>
      )
      const header = container.querySelector('[data-slot="card-header"]')
      expect(header).toBeInTheDocument()
      expect(screen.getByText('Header content')).toBeInTheDocument()
    })

    it('should apply proper classes', () => {
      const { container } = render(
        <Card>
          <CardHeader>Header</CardHeader>
        </Card>
      )
      const header = container.querySelector('[data-slot="card-header"]')
      expect(header?.className).toContain('px-6')
    })
  })

  describe('CardTitle', () => {
    it('should render card title', () => {
      render(
        <Card>
          <CardHeader>
            <CardTitle>My Title</CardTitle>
          </CardHeader>
        </Card>
      )
      expect(screen.getByText('My Title')).toBeInTheDocument()
    })

    it('should have heading styles', () => {
      const { container } = render(
        <Card>
          <CardHeader>
            <CardTitle>Title</CardTitle>
          </CardHeader>
        </Card>
      )
      const title = container.querySelector('[data-slot="card-title"]')
      expect(title?.className).toContain('font-heading')
      expect(title?.className).toContain('font-medium')
    })
  })

  describe('CardDescription', () => {
    it('should render card description', () => {
      render(
        <Card>
          <CardHeader>
            <CardDescription>My description</CardDescription>
          </CardHeader>
        </Card>
      )
      expect(screen.getByText('My description')).toBeInTheDocument()
    })

    it('should have muted styling', () => {
      const { container } = render(
        <Card>
          <CardHeader>
            <CardDescription>Description</CardDescription>
          </CardHeader>
        </Card>
      )
      const description = container.querySelector('[data-slot="card-description"]')
      expect(description?.className).toContain('text-muted-foreground')
    })
  })

  describe('CardContent', () => {
    it('should render card content', () => {
      render(
        <Card>
          <CardContent>Main content</CardContent>
        </Card>
      )
      expect(screen.getByText('Main content')).toBeInTheDocument()
    })

    it('should support nested elements', () => {
      render(
        <Card>
          <CardContent>
            <div data-testid="nested">Nested element</div>
          </CardContent>
        </Card>
      )
      expect(screen.getByTestId('nested')).toBeInTheDocument()
    })
  })

  describe('CardFooter', () => {
    it('should render card footer', () => {
      render(
        <Card>
          <CardContent>Content</CardContent>
          <CardFooter>Footer</CardFooter>
        </Card>
      )
      expect(screen.getByText('Footer')).toBeInTheDocument()
    })

    it('should support action buttons in footer', () => {
      render(
        <Card>
          <CardContent>Content</CardContent>
          <CardFooter>
            <button>Cancel</button>
            <button>Save</button>
          </CardFooter>
        </Card>
      )
      expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /save/i })).toBeInTheDocument()
    })
  })

  describe('Complete Card Structure', () => {
    it('should render complete card with all sections', () => {
      const { container } = render(
        <Card>
          <CardHeader>
            <CardTitle>Title</CardTitle>
            <CardDescription>Description</CardDescription>
          </CardHeader>
          <CardContent>Main content goes here</CardContent>
          <CardFooter>
            <button>Action</button>
          </CardFooter>
        </Card>
      )

      expect(screen.getByText('Title')).toBeInTheDocument()
      expect(screen.getByText('Description')).toBeInTheDocument()
      expect(screen.getByText('Main content goes here')).toBeInTheDocument()
      expect(screen.getByRole('button')).toBeInTheDocument()

      const card = container.querySelector('[data-slot="card"]')
      expect(card).toBeInTheDocument()
    })

    it('should maintain proper spacing', () => {
      const { container } = render(
        <Card>
          <CardHeader>
            <CardTitle>Title</CardTitle>
          </CardHeader>
          <CardContent>Content</CardContent>
          <CardFooter>Footer</CardFooter>
        </Card>
      )

      const card = container.querySelector('[data-slot="card"]')
      expect(card?.className).toContain('gap-6')
    })
  })
})
